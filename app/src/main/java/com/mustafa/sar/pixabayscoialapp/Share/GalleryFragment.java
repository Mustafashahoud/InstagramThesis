package com.mustafa.sar.pixabayscoialapp.Share;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.mustafa.sar.pixabayscoialapp.Profile.AccountSettingActivity;
import com.mustafa.sar.pixabayscoialapp.R;
import com.mustafa.sar.pixabayscoialapp.utilities.FileDirectory;
import com.mustafa.sar.pixabayscoialapp.utilities.HelperForGettingContentOfDirectories;
import com.mustafa.sar.pixabayscoialapp.utilities.UniversalImageLoader;
import com.mustafa.sar.pixabayscoialapp.utilities.gallery.EqualSpacingItemDecoration;
import com.mustafa.sar.pixabayscoialapp.utilities.gallery.RecycleViewAdapter;

import java.util.ArrayList;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";
    private GridView gridView;
    private ProgressBar mProgressBar;
    private ImageView galleryImage;
    private Spinner spinner;
    private ArrayList<String> imgPaths;
    public String selectedImg;

    //Constants
    private static final String mAppend = "file:/";

    RecyclerView recyclerView;

    private ArrayList<String> directories;
    private ArrayList<String> directoryNames;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        Log.d(TAG, "onCreateView: started.");
        System.out.println();

        galleryImage = (ImageView) view.findViewById(R.id.galleryImageView);
        gridView = (GridView) view.findViewById(R.id.gridView);
        spinner = (Spinner) view.findViewById(R.id.spinnerDirectory);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        directories = new ArrayList<>();
        directoryNames = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        ImageView shareClose = (ImageView) view.findViewById(R.id.closeImageView);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the gallery fragment.");
                getActivity().finish();
            }
        });

        TextView nextScreen = (TextView) view.findViewById(R.id.nextTextView);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share screen.");
                if (isRootTask()){ // True means that we did NOT come from EditProfile by pressing change profile Photo
                    Intent selectedImgIntent = new Intent(getActivity(), SelectedImgActivity.class);
                    selectedImgIntent.putExtra("SelectedImg", selectedImg);
                    startActivity(selectedImgIntent);
                }else{
                    Intent selectedImgIntent = new Intent(getActivity(), AccountSettingActivity.class);
                    selectedImgIntent.putExtra("SelectedImg", selectedImg);
                    selectedImgIntent.putExtra("return_to_fragment", getString(R.string.edit_profile_fragment));
                    startActivity(selectedImgIntent);
                    getActivity().fileList();
                }

            }
        });
        init();
        return view;
    }

    public boolean isRootTask(){
        if (((ShareActivity)getActivity()).getIntentFlag() == 0){
            return  true;
        }
        else return false;
    }

    private void init() {
        //Checking other folders inside /storage/emulated/0/pictures

        FileDirectory fileDirectory = new FileDirectory();
        //To add all the folders "Directories" inside the /storage/emulated/0/pictures
        if (HelperForGettingContentOfDirectories.getDirectoryPaths(fileDirectory.PICTURES) != null
        && HelperForGettingContentOfDirectories.getDirectoryPaths(fileDirectory.WHATS_APP) != null ) {
            //Just for showing in the spinner
            directoryNames.addAll(HelperForGettingContentOfDirectories.getDirectoryNames(fileDirectory.PICTURES));
            directoryNames.addAll(HelperForGettingContentOfDirectories.getDirectoryNames(fileDirectory.WHATS_APP));
            //the actual paths
            directories = HelperForGettingContentOfDirectories.getDirectoryPaths(fileDirectory.PICTURES);
            directories.addAll(HelperForGettingContentOfDirectories.getDirectoryPaths(fileDirectory.WHATS_APP));
        }
        //To add all the photos
        directoryNames.add(obtainingLastWordInDir(fileDirectory.CAMERA));
        directoryNames.add(obtainingLastWordInDir(fileDirectory.WHATS_APP));
        directories.add(fileDirectory.CAMERA);
        directories.add(fileDirectory.WHATS_APP);

        int slectionNum = 0;
        for (int i = 0; i < directoryNames.size(); i++){
            if (directoryNames.get(i).equals("Camera")){
                slectionNum = i;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, directoryNames);

        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(slectionNum);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                setupRecycleView(directories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Obtaining last word in path string.
     *
     * @param path the path
     * @return the string
     */
    public String obtainingLastWordInDir(String path) {
        String lastWord = path.substring(path.lastIndexOf("/") + 1);
        return lastWord;
    }

    private void setupRecycleView(String selectedPath) {

        int recyclerWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = recyclerWidth / 3;
        recyclerView.setHasFixedSize(true);

        /*
         * for setting a divider between the items or the views in The recycleView which can be done in two ways
         *the first one is by creating a new class that extends RecyclerView.ItemDecoration and overriding the method
         * getItemOffsets()
         * */
        recyclerView.addItemDecoration(new EqualSpacingItemDecoration(3, EqualSpacingItemDecoration.GRID));

        /*
         * OR by using the new class DividerItemDecoration */

        /*DividerItemDecoration decoration = new DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL );
        recyclerView.addItemDecoration(decoration);
        DividerItemDecoration decoration2 = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL );
        recyclerView.addItemDecoration(decoration2);*/

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(layoutManager);
        imgPaths = HelperForGettingContentOfDirectories.getFilesPaths(selectedPath);

        RecycleViewAdapter adapter = new RecycleViewAdapter(getActivity(), R.layout.layout_grid_imageview, "file:/",
                imgPaths, new RecycleViewAdapter.CustomOnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                //Listener for showing the selected pic form the gallery
                // pass the view and position of the clicked items
                UniversalImageLoader.setImage(imgPaths.get(position), galleryImage, mProgressBar, mAppend);
                selectedImg = imgPaths.get(position);
            }
        });
        recyclerView.setAdapter(adapter);

        //IN CASE we wanna use gridView with ViewHoler instead of RecyclerView
        //GridImageAdapter adapter = new GridImageAdapter(context , R.layout.layout_grid_imageview ,"",imgs);
        //gridView.setAdapter(adapter);
        try {
            //Populate the first big image view in the fragment_gallery layout when the gallery fragment is inflated
            if (imgPaths.size() > 0) {
                UniversalImageLoader.setImage(imgPaths.get(0), galleryImage, mProgressBar, mAppend);
                selectedImg = imgPaths.get(0);
            }
        }catch (ArrayIndexOutOfBoundsException e){
            Log.d(TAG, "setupRecycleView:  ArrayIndexOutOfBoundsException" +e.getMessage());
        }



    }

}
