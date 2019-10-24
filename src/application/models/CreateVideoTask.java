package application.models;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * A task to be run in the background for creating video
 * @author Tommy Shi and Justin Teo
 */
public class CreateVideoTask extends Task<Void> {

	private Creation _creation;
	private int _numberOfImages;
	private boolean _includeMusic;
	private CreationListModel _model;
	private List<Integer> imagePositionToInclude;


	public CreateVideoTask(Creation creation, CreationListModel model, List<Integer> positions, boolean includeMusic) {
		_creation = creation;
		_model = model;
		_includeMusic = includeMusic;
		imagePositionToInclude = positions;
		_numberOfImages = positions.size();
	}

	/**
	 * The main method that is done in the background thread
	 * @return null
	 * @throws Exception
	 */
	@Override
	protected Void call() throws Exception {
		// set up paths to all necessary directories
		String path = System.getProperty("user.dir") + System.getProperty("file.separator");
		String pathToCreation = path + "creations" + System.getProperty("file.separator");
		String pathToQuiz = path +"quiz" + System.getProperty("file.separator");
		String termInVideo = _creation.getSearchTerm();

		double duration = findDuration(path);
		duration = duration/_numberOfImages; // split total duration of the video evenly between how many images

		setUpImage(path, duration); // call helper method which sets up a text file for ffmpeg command
		createSlideShow(path, termInVideo); // call helper method which creates the slideshow of the images

		if (_includeMusic) {
			addMusicToSlideShow();
		}

		makeCreationVideo(pathToCreation); // merge audio and slideshow to make a video
		makeQuizVideo(path, pathToQuiz, termInVideo); // make the test video
		tidyUp();

		// update model
		Platform.runLater(() -> _model.create(_creation));

		return null;
	}

	/**
	 * Find the duration of the audio
	 * @param path the path to working directory
	 * @return the duration of the audio
	 * @throws Exception
	 */
	private double findDuration(String path) throws Exception{
		String command = "soxi -D \"" + path + "combine.wav\"";
		BashCommands findDuration = new BashCommands(command);
		findDuration.startBashProcess();
		findDuration.getProcess().waitFor();
		double duration = Double.parseDouble(findDuration.getStdout());
		return duration;
	}

	/**
	 * Set up the images
	 * @param path path to working directory
	 * @param duration duration of the audio
	 */
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

	/**
	 * Create a slide show of the images and search term
	 * @param path path to working directory
	 * @param term the search term
	 * @throws InterruptedException
	 */
	private void createSlideShow(String path, String term) throws InterruptedException {
		String command1 = "ffmpeg -y -f concat -safe 0 -i "+path+"commands.txt"+ " -pix_fmt yuv420p -r 25 -vf 'scale=trunc(iw/2)*2:trunc(ih/2)*2' " +path+"video.mp4";
		String command2 = "ffmpeg -y -i "+path+"video.mp4 "+ "-vf \"drawtext=fontfile=:fontsize=50:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + term + "'\" "+"-r 25 "+path+"good.mp4";

		String command = command1+";"+command2;

		BashCommands create = new BashCommands(command);
		create.startBashProcess();
		create.getProcess().waitFor();
	}

	/**
	 * Add background music to the slide show
	 * @throws InterruptedException
	 */
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

	/**
	 * Tidy up and delete temporary files
	 * @throws InterruptedException
	 */
	private void tidyUp() throws InterruptedException {
		String command = "rm -f *.jpg ; rm -f *.wav ; rm -f *.mp4 ; rm -f commands.txt ; rm -f *.scm ; rm -f subtitles.srt";
		BashCommands tidyUp = new BashCommands(command);
		tidyUp.startBashProcess();
		tidyUp.getProcess().waitFor();
	}

	/**
	 * Make the final creation video, merges audio and video
	 * @param pathToCreation path to the creations folder
	 * @throws InterruptedException
	 */
	private void makeCreationVideo(String pathToCreation) throws InterruptedException {
		String command = "ffmpeg -y -i \"good.mp4\" -i \"combine.wav\" " + _creation.getVideoName() + ".mp4";
		BashCommands merge = new BashCommands(command); //merge audio and video
		merge.startBashProcess();
		merge.getProcess().waitFor();

		// add the subtitles into video
		command = "ffmpeg -i " + _creation.getVideoName() + ".mp4 -vf subtitles=subtitles.srt " + pathToCreation + _creation.getVideoName() + ".mp4";
		BashCommands subtitles = new BashCommands(command);
		subtitles.startBashProcess();
		subtitles.getProcess().waitFor();
	}

	/**
	 * Make the video for quiz, without the search term in the video
	 * @param path path to working directory
	 * @param pathToQuiz path to quiz folder
	 * @param term the search term
	 * @throws InterruptedException
	 */
	private void makeQuizVideo(String path, String pathToQuiz, String term) throws InterruptedException {
		String command1 = "ffmpeg -y -f concat -safe 0 -i "+path+"commands.txt"+ " -pix_fmt yuv420p -r 25 -vf 'scale=trunc(iw/2)*2:trunc(ih/2)*2' " +path+"video.mp4";
		String command2 = "ffmpeg -y -i "+path+"video.mp4 -r 25 "+path+"good.mp4";
		String command = command1+";"+command2;

		BashCommands createNoTerm = new BashCommands(command);
		createNoTerm.startBashProcess();
		createNoTerm.getProcess().waitFor();

		command = "ffmpeg -y -i \"good.mp4\" -i \"combine.wav\" " + pathToQuiz + term + "quiz.mp4";
		BashCommands merge = new BashCommands(command); // merge video and audio
		merge.startBashProcess();
		merge.getProcess().waitFor();
	}
}
