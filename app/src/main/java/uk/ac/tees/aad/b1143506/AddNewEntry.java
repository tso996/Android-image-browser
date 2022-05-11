package uk.ac.tees.aad.b1143506;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.ByteArrayOutputStream;

import uk.ac.tees.aad.b1143506.Model.ToDoModel;
import uk.ac.tees.aad.b1143506.Utils.DatabaseHandler;

//needs to handle image browsing here
public class AddNewEntry extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";
    private EditText entryText;
    private Button doneButton;
    private Button cameraButton;
    private ImageView cameraImage;
    private Button locationButton;


    private DatabaseHandler db;

    Bitmap bitmapImage;

    public static AddNewEntry newInstance(){
        return new AddNewEntry();
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
        entryText = requireView().findViewById(R.id.entry_text);
        doneButton = getView().findViewById(R.id.Done);
        entryText.setHint("Type here");
        cameraButton = getView().findViewById(R.id.camera_button);
        cameraImage = getView().findViewById(R.id.camera_image);
        locationButton = getView().findViewById(R.id.location_button);

        doneButton.setEnabled(false);
        cameraButton.setEnabled(false);
        locationButton.setEnabled(false);


        boolean isUpdate = false;

        final Bundle bundle = getArguments();
        if(bundle != null){
            isUpdate = true;
            String task = bundle.getString("task");
            entryText.setText(task);
            assert task != null;

        }

        db = new DatabaseHandler(getActivity());
        db.openDatabase();

        entryText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")){
                    doneButton.setEnabled(false);
                    cameraButton.setEnabled(false);
                    locationButton.setEnabled(false);

                }
                else{
                    doneButton.setEnabled(true);
                    cameraButton.setEnabled(true);
                    locationButton.setEnabled(true);


                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        final boolean finalIsUpdate = isUpdate;
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean imageStatus = false;
                //converting bitmap to byte array
                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                if(bitmapImage!=null) { 
                    bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, byteArray);
                    Log.d("the Byte array from addnewtask: ", byteArray.toString());
                    imageStatus = true;
                }
                //===========
                String text = entryText.getText().toString();
                String location;
                location = MainActivity.currentLocation;
                if(!(MainActivity.chosenCustomisedLocation ==" ")){
                    location = MainActivity.chosenCustomisedLocation;
                }
                if(finalIsUpdate){//Editing
                    db.updateTask(bundle.getInt("id"), text);
                    if (imageStatus)
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
                MainActivity.chosenCustomisedLocation =" ";
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
                            cameraImage.setImageBitmap(bitmapImage);
                            cameraImage.setVisibility(View.VISIBLE);
                        }
                    }
                });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraActivityLauncher.launch(i);

            }
        });

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity().getApplicationContext(),MapsActivity.class);
                startActivity(i);
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
