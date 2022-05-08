package uk.ac.tees.aad.b1143506.Adapters;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import uk.ac.tees.aad.b1143506.AddNewTask;
import uk.ac.tees.aad.b1143506.MainActivity;
import uk.ac.tees.aad.b1143506.Model.ToDoModel;
import uk.ac.tees.aad.b1143506.R;
import uk.ac.tees.aad.b1143506.Utils.DatabaseHandler;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private List<ToDoModel> todoList;
    private DatabaseHandler db;
    private MainActivity activity;

    public ToDoAdapter(DatabaseHandler db, MainActivity activity) {
        this.db = db;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        db.openDatabase();

        final ToDoModel item = todoList.get(position);
        holder.task.setText(item.getTask());
        holder.location.setText(item.getLocation());
        //this marks the entry as complete or not
        holder.task.setChecked(toBoolean(item.getStatus()));
        Log.d ("viewBinder calling: ","here");

        if(toBoolean(item.getStatus())) {
            holder.strike.setVisibility(View.VISIBLE);
        }else{
            holder.strike.setVisibility(View.INVISIBLE);
        }

        byte[] b = item.getImage();
        if(b!=null) {
            Bitmap bitmapImage = BitmapFactory.decodeByteArray(b, 0, b.length);
            Log.d("bitmapImage from the ToDoAdapter: ", Integer.toString(b.length));
            holder.image.setVisibility(View.VISIBLE);
            holder.image.setImageBitmap(bitmapImage);
        }else{
            holder.image.setVisibility(View.GONE);

        }
        holder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d ("check status: ","clicked");
                if (isChecked) {
                    Log.d ("check status is checked: ","clicked");
                    db.updateStatus(item.getId(), 1);
                    holder.strike.setVisibility(View.VISIBLE);

                } else {
                    db.updateStatus(item.getId(), 0);
                    holder.strike.setVisibility(View.INVISIBLE);
                }
            }

        });
    }

    private boolean toBoolean(int n) {
        return n != 0;
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public Context getContext() {
        return activity;
    }

    public void setTasks(List<ToDoModel> todoList) {
        this.todoList = todoList;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        ToDoModel item = todoList.get(position);
        db.deleteTask(item.getId());
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    public void editItem(int position) {
        ToDoModel item = todoList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId());
        bundle.putString("task", item.getTask());
        bundle.putString("location", item.getTask());
        bundle.putByteArray("image",item.getImage());
        AddNewTask fragment = new AddNewTask();
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), AddNewTask.TAG);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;
        TextView location;
        ImageView image;
        ImageView strike;
        ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.check_box);
            location = view.findViewById(R.id.location_holder);
            image = view.findViewById(R.id.imageView);
            strike = view.findViewById(R.id.strike_check);

        }
    }
}
