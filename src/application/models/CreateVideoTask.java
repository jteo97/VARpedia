package application.models;


import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class CreateVideoTask extends Task<Void> {

    private String _nameOfCreation;
    private int _numberOfImages;
    private String _searchTerm;
    private String _wikiSearch;
    private CreationListModel _model;


    public CreateVideoTask(String name, int number, String searchTerm, String wikisearch, CreationListModel model) {
        this._nameOfCreation = name;
        this._numberOfImages = number;
        this._searchTerm = searchTerm;
        this._wikiSearch = wikisearch;
        _model = model;
    }

    @Override
    protected Void call() throws Exception {
        String _path = System.getProperty("user.dir") + System.getProperty("file.separator");
        String _pathToCreation = _path + "creations" + System.getProperty("file.separator");
        String _term = _searchTerm;

//        String command = "duration=`soxi -D \"" + _path + "/combine.wav\"` ; " +
//                "ffmpeg -framerate 5/\"$duration\" -f image2 -s 800x600 -i \"" + _path + "/image%01d.jpg\" -vcodec libx264 -crf 25 -pix_fmt yuv420p -vf \"pad=ceil(iw/2)*2:ceil(ih/2)*2\" -r 25 \"" + _path + "/slideshow.mp4\" ; " +
//                "ffmpeg -y -i \"" + _path + "/slideshow.mp4\" -vf \"drawtext=fontfile=:fontsize=30:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + _wikiSearch + "'\" \"" + _path + "/video.mp4\"";

        String command = "soxi -D \"" + _path + "combine.wav\"";
        BashCommands findDuration = new BashCommands(command);
        findDuration.startBashProcess();
        findDuration.getProcess().waitFor();
        double duration = Double.parseDouble(findDuration.getStdout());

        System.out.println(duration);
        duration = duration/_numberOfImages;
        System.out.println(duration);

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(_path+"cmd.txt", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (int i = 1; i < _numberOfImages+1; i++) {
            if (i==_numberOfImages) {
                writer.println("file "+_path+"image"+i+".jpg");
                writer.println("duration "+duration);
                writer.println("file "+_path+"image"+i+".jpg");
                break;
            }
            writer.println("file "+_path+"image"+i+".jpg");
            writer.println("duration "+duration);
        }
        writer.close();

        String command1 = "ffmpeg -y -f concat -safe 0 -i "+_path+"cmd.txt"+ " -pix_fmt yuv420p -r 25 -vf 'scale=trunc(iw/2)*2:trunc(ih/2)*2' " +_path+"video.mp4";
        String command2 = "ffmpeg -y -i "+_pathToCreation+"/"+_nameOfCreation+"/"+"video.mp4 "+ "-vf \"drawtext=fontfile=:fontsize=30:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='"+_term+"'\" "+"-r 25 "+_pathToCreation+"/"+_nameOfCreation+"/"+_nameOfCreation+".mp4";
        command = command1+";"+command2;

        BashCommands create = new BashCommands(command);
        create.startBashProcess();
        create.getProcess().waitFor();

        System.out.println("DONE WITH CREATING");

        command = "ffmpeg -y -i \"video.mp4\" -i \"combine.wav\" " + _pathToCreation + _nameOfCreation + ".mp4";
        BashCommands merge = new BashCommands(command);
        merge.startBashProcess();
        merge.getProcess().waitFor();

        System.out.println("DONE WITH MERGING");

        command = "rm -f *.jpg ; rm -f *.wav ; rm -f video.mp4 ; rm -f slideshow.mp4 ; rm -f cmd.txt; rm -f *.scm";
        BashCommands tidyUp = new BashCommands(command);
        tidyUp.startBashProcess();
        tidyUp.getProcess().waitFor();
        
        // update model
        Platform.runLater(new Runnable() {

			@Override
			public void run() {
				_model.create(_nameOfCreation+ ".mp4");
			}
        	
        });
        

        System.out.println("DONE WITH TIDY UP");
        return null;
    }
}
