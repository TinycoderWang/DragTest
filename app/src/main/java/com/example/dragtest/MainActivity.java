package com.example.dragtest;

import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView drag;
    TextView view1;
    TextView view2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drag = findViewById(R.id.drag);
        view1 = findViewById(R.id.view1);
        view2 = findViewById(R.id.view2);

        drag.setOnLongClickListener(v -> {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(null,shadowBuilder,null,0);
            return false;
        });

        view1.setOnDragListener((v, event) -> {
            switch (event.getAction()){
                case DragEvent.ACTION_DRAG_ENTERED:
                    view1.setText("ACTION_DRAG_ENTERED");
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    view1.setText("ACTION_DRAG_EXITED");
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    view1.setText("");
                    break;
            }
            return true;
        });

        view2.setOnDragListener((v, event) -> {
            switch (event.getAction()){
                case DragEvent.ACTION_DRAG_ENTERED:
                    view2.setText("ACTION_DRAG_ENTERED");
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    view2.setText("ACTION_DRAG_EXITED");
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    view2.setText("");
                    break;
            }
            return true;
        });
    }
}