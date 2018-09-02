package masters.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import masters.features.ContinousFeature;
import masters.features.Feature;


public class Helper {
	public static List<Double> initFixedSizedListDouble(int listSize) {
		List<Double> result = new ArrayList<Double>();
		for (int i = 0; i < listSize; i++) {
			result.add(0.0);
		}
		return result;
	}
	public static List<Feature> initFixedSizedListContinuousFeature(int listSize) {
		List<Feature> result = new ArrayList<Feature>();
		for (int i = 0; i < listSize; i++) {
			result.add(new ContinousFeature(0.0, -1));
		}
		return result;
	}
	public static List<Integer> initFixedSizedListInteger(int listSize) {
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < listSize; i++) {
			result.add(0);
		}
		return result;
	}

	public static void printList(List<Double> labelProbabilities) {
		System.out.print("( ");
		for (Double d : labelProbabilities) {
			System.out.print(d + " ");
		}
		System.out.println(" )");
	}
	public static void playSound(final String url) {
        Clip clip;
		try {
			clip = AudioSystem.getClip();
			File file = new File(System.getProperty("user.dir") + "\\src\\" + url);
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(file);
			clip.open(inputStream);
			clip.start(); 
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
