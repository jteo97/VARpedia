package application.models;


import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class CreateVideoTask extends Task<Void> {

	private String _nameOfCreation;
	private int _numberOfImages;
	private String _wikiSearch;
	private boolean _includeMusic;
	private CreationListModel _model;
	private List<Integer> imagePositionToInclude = new ArrayList<>();


	public CreateVideoTask(String name, ArrayList<CheckBox> checkBoxes, String wikisearch, CreationListModel model, boolean includeMusic) {
		_nameOfCreation = name;
		_wikiSearch = wikisearch;
		_model = model;
		_includeMusic = includeMusic;

		int count = 1;
		for (CheckBox c: checkBoxes) {
			if (c.isSelected()) {
				imagePositionToInclude.add(count);

			}
			count++;
		}
		_numberOfImages = imagePositionToInclude.size();
	}

	@Override
	protected Void call() throws Exception {
		String _path = System.getProperty("user.dir") + System.getProperty("file.separator");
		String _pathToCreation = _path + "creations" + System.getProperty("file.separator");
		String _pathToQuiz = _path +"quiz" + System.getProperty("file.separator");
		String _term = _wikiSearch;

		double duration = findDuration(_path);
		duration = duration/_numberOfImages;

		setUpImage(_path, duration);
		createSlideShow(_path, _term);

		if (_includeMusic) {
			addMusicToSlideShow();
		}

		makeCreationVideo(_pathToCreation);
		makeQuizVideo(_path, _pathToQuiz, _term);
		tidyup();

		// update model
		Platform.runLater(() -> _model.create(_nameOfCreation+ ".mp4"));

		return null;
	}

	private double findDuration(String path) throws Exception{
		String command = "soxi -D \"" + path + "combine.wav\"";
		BashCommands findDuration = new BashCommands(command);
		findDuration.startBashProcess();
		findDuration.getProcess().waitFor();
		double duration = Double.parseDouble(findDuration.getStdout());
		return duration;
	}

	private void setUpImage(String path, double duration) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(path+"commands.txt", "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < imagePositionToInclude.size(); i++) {
			if (i == imagePositionToInclude.size()-1) {
				writer.println("file " + path + "image" + imagePositionToInclude.get(i) + ".jpg");
				writer.println("duration " + duration);
				writer.println("file " + path + "image" + imagePositionToInclude.get(i) + ".jpg");
				break;
			}
			writer.println("file " + path + "image" + imagePositionToInclude.get(i) + ".jpg");
			writer.println("duration " + duration);
		}
		writer.close();
	}

	private void createSlideShow(String path, String term) throws InterruptedException {
		String command1 = "ffmpeg -y -f concat -safe 0 -i "+path+"commands.txt"+ " -pix_fmt yuv420p -r 25 -vf 'scale=trunc(iw/2)*2:trunc(ih/2)*2' " +path+"video.mp4";
		String command2 = "ffmpeg -y -i "+path+"video.mp4 "+ "-vf \"drawtext=fontfile=:fontsize=50:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + term + "'\" "+"-r 25 "+path+"good.mp4";

		String command = command1+";"+command2;

		BashCommands create = new BashCommands(command);
		create.startBashProcess();
		create.getProcess().waitFor();
	}

	private void addMusicToSlideShow() throws InterruptedException {
		// rename original file so ffmpeg won't get an error
		File original = new File("combine.wav");
		File renamed = new File("combine_c.wav");
		original.renameTo(renamed);

		// add background music to the audio
		BashCommands addMusic = new BashCommands("ffmpeg -i combine_c.wav -i \"musics" +
				System.getProperty("file.separator") + "panumoon_-_another_perspective_2.mp3\" -filter_complex amix=inputs=2:duration=first:dropout_transition=0 combine.wav");
		addMusic.startBashProcess();
		addMusic.getProcess().waitFor();
	}
	
	private void tidyup() throws InterruptedException {
		String command = "rm -f *.jpg ; rm -f *.wav ; rm -f *.mp4 ; rm -f commands.txt ; rm -f *.scm ; rm -f subtitles.srt";
		BashCommands tidyUp = new BashCommands(command);
		tidyUp.startBashProcess();
		tidyUp.getProcess().waitFor();
	}
	
	private void makeCreationVideo(String pathToCreation) throws InterruptedException {
		String command = "ffmpeg -y -i \"good.mp4\" -i \"combine.wav\" " + _nameOfCreation + ".mp4";
		BashCommands merge = new BashCommands(command);
		merge.startBashProcess();
		merge.getProcess().waitFor();

		command = "ffmpeg -i " + _nameOfCreation + ".mp4 -vf subtitles=subtitles.srt " + pathToCreation + _nameOfCreation + ".mp4";
		BashCommands subtitles = new BashCommands(command);
		subtitles.startBashProcess();
		subtitles.getProcess().waitFor();
	}
	
	private void makeQuizVideo(String path, String pathToQuiz, String term) throws InterruptedException {
		String command1 = "ffmpeg -y -f concat -safe 0 -i "+path+"commands.txt"+ " -pix_fmt yuv420p -r 25 -vf 'scale=trunc(iw/2)*2:trunc(ih/2)*2' " +path+"video.mp4";
		String command2 = "ffmpeg -y -i "+path+"video.mp4 -r 25 "+path+"good.mp4";
		String command = command1+";"+command2;

		BashCommands createNoTerm = new BashCommands(command);
		createNoTerm.startBashProcess();
		createNoTerm.getProcess().waitFor();

		command = "ffmpeg -y -i \"good.mp4\" -i \"combine.wav\" " + pathToQuiz + term + "quiz.mp4";
		BashCommands merge = new BashCommands(command);
		merge.startBashProcess();
		merge.getProcess().waitFor();
		
	}
}
