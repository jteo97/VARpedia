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
        String _term = _wikiSearch;

        String command = "soxi -D \"" + _path + "combine.wav\"";
        BashCommands findDuration = new BashCommands(command);
        findDuration.startBashProcess();
        findDuration.getProcess().waitFor();
        double duration = Double.parseDouble(findDuration.getStdout());

        duration = duration/_numberOfImages;

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(_path+"commands.txt", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (int i = 1; i < _numberOfImages+1; i++) {
            if (i==_numberOfImages) {
                writer.println("file " + _path + "image" + i + ".jpg");
                writer.println("duration " + duration);
                writer.println("file " + _path + "image" + i + ".jpg");
                break;
            }
            writer.println("file "+_path+"image"+i+".jpg");
            writer.println("duration "+duration);
        }
        writer.close();

        String command1 = "ffmpeg -y -f concat -safe 0 -i "+_path+"commands.txt"+ " -pix_fmt yuv420p -r 25 -vf 'scale=trunc(iw/2)*2:trunc(ih/2)*2' " +_path+"video.mp4";
        String command2 = "ffmpeg -y -i "+_path+"video.mp4 "+ "-vf \"drawtext=fontfile=:fontsize=30:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + _term + "'\" "+"-r 25 "+_path+"good.mp4";

        command = command1+";"+command2;

        BashCommands create = new BashCommands(command);
        create.startBashProcess();
        create.getProcess().waitFor();

        command = "ffmpeg -y -i \"good.mp4\" -i \"combine.wav\" " + _pathToCreation + _nameOfCreation + ".mp4";
        BashCommands merge = new BashCommands(command);
        merge.startBashProcess();
        merge.getProcess().waitFor();

        command = "rm -f *.jpg ; rm -f *.wav ; rm -f *.mp4 ; rm -f commands.txt ; rm -f *.scm";

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
        
        return null;
    }
}
