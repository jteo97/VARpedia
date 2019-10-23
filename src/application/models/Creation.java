package application.models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Tommy Shi
 *
 */
public class Creation {

	private String _videoName;
	private String _searchTerm;
	private String _searchResult;
	
	public Creation(String searchTerm, String searchResult) {
		_searchTerm = searchTerm;
		_searchResult = searchResult;
	}
	
	public Creation(String videoName) {
		_videoName = videoName;
	}
	
	public void combineAudios(InputStream stdout, String path) throws Exception{
		PrintWriter writer = new PrintWriter("subtitles.srt", "UTF-8");
		BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
        String cmd = "sox ";
        String line;
        int subtitleSection = 1;
        String old = null;
        while ((line = stdoutBuffered.readLine()) != null) {
            cmd += line + " ";

            writer.println(subtitleSection);
            String command = "soxi -D \"" + path + line + "\"";
            BashCommands findDuration = new BashCommands(command);
            findDuration.startBashProcess();
            findDuration.getProcess().waitFor();
            double duration = Double.parseDouble(findDuration.getStdout());
            duration += 0.1;
            DecimalFormat df = new DecimalFormat("##.##");
            String formattedDur = df.format(duration);
            formattedDur = formattedDur.replaceFirst("[.]", ",");

            if (subtitleSection == 1) {
                if (formattedDur.length() == 4) {
                    writer.println("00:00:00,00 --> " + "00:00:0" + formattedDur);
                } else {
                    writer.println("00:00:00,00 --> " + "00:00:" + formattedDur);
                }
            } else {
                old = old.replaceFirst("[,]", ".");
                double temp = Double.parseDouble(old);
                formattedDur = formattedDur.replaceFirst("[,]", ".");
                double temp2 = Double.parseDouble(formattedDur);
                temp2 = temp2 + temp;
                old = old.replaceFirst("[.]", ",");
                df = new DecimalFormat("##.##");
                formattedDur = df.format(temp2);
                formattedDur = formattedDur.replaceFirst("[.]", ",");
                if (old.length() == 4) {
                    if (formattedDur.length() == 4) {
                        writer.println("00:00:0" + old + " --> " + "00:00:0" + formattedDur);
                    } else {
                        writer.println("00:00:0" + old + " --> " + "00:00:" + formattedDur);
                    }

                } else {
                    if (formattedDur.length() == 4) {
                        writer.println("00:00:" + old + " --> " + "00:00:0" + formattedDur);
                    } else {
                        writer.println("00:00:" + old + " --> " + "00:00:" + formattedDur);
                    }
                }
            }
            line = line.substring(0, line.length() - 4);
            BufferedReader br = new BufferedReader(new FileReader(line + ".txt"));
            String subtitle = br.readLine();
            br.close();
            writer.println(subtitle);
            writer.println();

            old = formattedDur;
            subtitleSection++;
        }

        writer.close();
        cmd += "combine.wav";
        BashCommands combine = new BashCommands(cmd);
        combine.startBashProcess();
        combine.getProcess().waitFor();
	}
	
	public CreateVideoTask createVideo(String name, CreationListModel model, List<Integer> positions, boolean includeMusic) {
		_videoName = name;
		ExecutorService team = Executors.newSingleThreadExecutor();
		CreateVideoTask createTask;
        if (includeMusic) {
            createTask = new CreateVideoTask(this, model, positions, true);
        } else {
            createTask = new CreateVideoTask(this, model, positions, true);
        }
        team.submit(createTask);
        return createTask;
	}
	
	public String getSearchTerm() {
		return _searchTerm;
	}
	
	public String getSearchResult() {
		return _searchResult;
	}
	
	public String getVideoName() {
		return _videoName;
	}

	@Override
	public String toString() {
		return _videoName;
	}
}
