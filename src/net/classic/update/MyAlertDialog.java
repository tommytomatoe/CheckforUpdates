package net.classic.update;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class MyAlertDialog extends DialogFragment {

    private static final String TAG = "UpdateAlertDialog";

	public static MyAlertDialog newInstance(int title, int message) {
    	MyAlertDialog  frag = new MyAlertDialog ();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("message", message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        int message = getArguments().getInt("message");
        
        AlertDialog.Builder builder;
        try {
        	builder =  new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
        } catch (NoSuchMethodError e) {
        	Log.e(TAG, "Older SDK, using old Builder");
        	builder =  new AlertDialog.Builder(getActivity());
		}
        //builder =  new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.alert_dialog_icon);

        if (title == R.string.update_available) {
        	builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Now", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	Intent i = new Intent(getActivity(), Update.class);
        	    		startActivity(i);
                    }
                });
            builder.setNegativeButton("Later",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //
                    }
                }
            );
        } else {
            builder.setTitle(title)
            .setMessage(message)
            .setNegativeButton(R.string.got_it,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //
                    }
                }
            );
        }
     
        return builder.create();
    }
}
