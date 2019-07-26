package com.thaliees.expandablelayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements ExpandableLayout.OnExpansionUpdateListener {
    // Define the states (same as in ExpandableLayout)
    private static final int COLLAPSED = 0;
    private static final int COLLAPSING = 1;
    private static final int EXPANDING = 2;
    private static final int EXPANDED = 3;

    private Button button_1;
    private Switch eSwitch;
    private ExpandableLayout expandable_1, expandable_2;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_1 = findViewById(R.id.button_1);
        eSwitch = findViewById(R.id.eSwitch);
        eSwitch.setText(getString(R.string.button_expanded));
        expandable_1 = findViewById(R.id.expandable_info);
        expandable_2 = findViewById(R.id.expandable_switch);
        text = findViewById(R.id.label_expansion);
        text.setText(getString(R.string.label_expansion, 0.0));

        button_1.setOnClickListener(effectToggle);
        eSwitch.setOnCheckedChangeListener(effect);
        // Use our Interface
        expandable_1.setOnExpansionUpdateListener(this);
        expandable_2.setOnExpansionUpdateListener(this);
    }

    private View.OnClickListener effectToggle = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            expandable_1.toggle();
        }
    };

    private CompoundButton.OnCheckedChangeListener effect = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) expandable_2.expand();
            else expandable_2.collapse();
        }
    };

    @Override
    public void onExpansionUpdate(View v, float expansionFraction, int state) {
        switch (v.getId()){
            case R.id.expandable_info:
                switch (state){
                    case COLLAPSED: button_1.setText(getString(R.string.button_expanded)); break;
                    case COLLAPSING: button_1.setText(getString(R.string.button_collapsing)); break;
                    case EXPANDED: button_1.setText(getString(R.string.button_collapsed)); break;
                    case EXPANDING: button_1.setText(getString(R.string.button_expanding)); break;
                }
                text.setText(getString(R.string.label_expansion, expansionFraction));
                break;

            case R.id.expandable_switch:
                if (state == COLLAPSED) eSwitch.setText(getString(R.string.button_expanded));
                if (state == EXPANDED) eSwitch.setText(getString(R.string.button_collapsed));
                text.setText(getString(R.string.label_expansion, expansionFraction));
                break;
        }
    }
}
