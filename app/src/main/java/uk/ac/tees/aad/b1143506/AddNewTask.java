package uk.ac.tees.aad.b1143506;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.ByteArrayOutputStream;

import uk.ac.tees.aad.b1143506.Model.ToDoModel;
import uk.ac.tees.aad.b1143506.Utils.DatabaseHandler;

//needs to handle image browsing here
public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";
    private EditText newTaskText;
    private Button newTaskSaveButton;
    private Button newTaskCameraButton;
    private ImageView newTaskCameraImage;

    private DatabaseHandler db;

    Bitmap bitmapImage;

    public static AddNewTask newInstance(){
        return new AddNewTask();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.new_task, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newTaskText = requireView().findViewById(R.id.newTaskText);
        newTaskSaveButton = getView().findViewById(R.id.newTaskButton);
        newTaskText.setHint("New task");
        newTaskCameraButton = getView().findViewById(R.id.camera_button);
        newTaskCameraImage = getView().findViewById(R.id.camera_image);
        newTaskSaveButton.setEnabled(false);
        newTaskCameraButton.setEnabled(false);

        boolean isUpdate = false;

        final Bundle bundle = getArguments();
        if(bundle != null){
            isUpdate = true;
            String task = bundle.getString("task");
            newTaskText.setText(task);
            assert task != null;
            if(task.length()>0){
                newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark));
                newTaskCameraButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark));
            }
        }

        db = new DatabaseHandler(getActivity());
        db.openDatabase();

        newTaskText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")){
                    newTaskSaveButton.setEnabled(false);
                    newTaskSaveButton.setTextColor(Color.GRAY);
                    newTaskCameraButton.setEnabled(false);
                    newTaskCameraButton.setTextColor(Color.GRAY);
                }
                else{
                    newTaskSaveButton.setEnabled(true);
                    newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark));
                    newTaskCameraButton.setEnabled(true);
                    newTaskCameraButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark));

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        final boolean finalIsUpdate = isUpdate;
        newTaskSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //converting bitmap to byte array

                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                bitmapImage.compress(Bitmap.CompressFormat.PNG,100,byteArray);
                Log.d("the Byte array from addnewtask: ",byteArray.toString());
                //===========
                String text = newTaskText.getText().toString();
                String location="";
                if(MainActivity.getLocationCheck) {
                    location = MainActivity.currentLocationAddress;
                }
                Log.d("this is the current activity: ",this.getClass().toString());
                if(finalIsUpdate){
                    db.updateTask(bundle.getInt("id"), text);
                    if (byteArray!=null)
                    db.updateImage(bundle.getInt("id"), byteArray.toByteArray());
                }
                else {
                    ToDoModel task = new ToDoModel();
                    task.setTask(text);
                    task.setLocation(location);
                    task.setImage(byteArray.toByteArray());
                    task.setStatus(0);
                    db.insertTask(task);
                }
                dismiss();
            }
        });

    //the camera handler
        ActivityResultLauncher<Intent> cameraActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            //call camera
                            bitmapImage = (Bitmap) data.getExtras().get("data");
                            newTaskCameraImage.setImageBitmap(bitmapImage);
                            newTaskCameraImage.setVisibility(View.VISIBLE);
                        }
                    }
                });

        newTaskCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraActivityLauncher.launch(i);

            }
        });

    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog){
        Activity activity = getActivity();
        if(activity instanceof uk.ac.tees.aad.b1143506.DialogCloseListener)
            ((uk.ac.tees.aad.b1143506.DialogCloseListener)activity).handleDialogClose(dialog);
    }
}
