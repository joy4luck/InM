package edu.mit.media.inm.status;
import edu.mit.media.inm.MajorFragment;
import edu.mit.media.inm.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 
public class UpdateFragment extends MajorFragment {
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_update, container, false);
         
        return rootView;
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}