package application.models;


import javafx.concurrent.Task;

public class CreateVideoTask extends Task<Void> {

    private String _nameOfCreation;
    private int _numberOfImages;
    private String _searchTerm;
    private String _wikiSearch;


    public CreateVideoTask(String name, int number, String searchTerm, String wikisearch) {
        this._nameOfCreation = name;
        this._numberOfImages = number;
        this._searchTerm = searchTerm;
        this._wikiSearch = wikisearch;
    }

    @Override
    protected Void call() throws Exception {
        String _path = System.getProperty("user.dir");
        String _pathToCreation = System.getProperty("user.dir") + System.getProperty("file.separator") + "creations" + System.getProperty("file.separator");
        String _term = _searchTerm;

        String command = "duration=`soxi -D \"" + _path + "/combine.wav\"` ; " +
                "ffmpeg -framerate 5/\"$duration\" -f image2 -s 800x600 -i \"" + _path + "/image%01d.jpg\" -vcodec libx264 -crf 25 -pix_fmt yuv420p -vf \"pad=ceil(iw/2)*2:ceil(ih/2)*2\" -r 25 \"" + _path + "/slideshow.mp4\" ; " +
                "ffmpeg -y -i \"" + _path + "/slideshow.mp4\" -vf \"drawtext=fontfile=:fontsize=30:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + _wikiSearch + "'\" \"" + _path + "/video.mp4\"";
        BashCommands create = new BashCommands(command);
        create.startBashProcess();
        create.getProcess().waitFor();

        System.out.println("DONE WITH CREATING");

        command = "ffmpeg -y -i \"video.mp4\" -i \"combine.wav\" " + _pathToCreation + _nameOfCreation + ".mp4";
        BashCommands merge = new BashCommands(command);
        merge.startBashProcess();
        merge.getProcess().waitFor();

        System.out.println("DONE WITH MERGING");

        command = "rm -f *.jpg ; rm -f *.wav ; rm -f video.mp4 ; rm -f slideshow.mp4";
        BashCommands tidyUp = new BashCommands(command);
        tidyUp.startBashProcess();
        tidyUp.getProcess().waitFor();

        return null;
    }
}
