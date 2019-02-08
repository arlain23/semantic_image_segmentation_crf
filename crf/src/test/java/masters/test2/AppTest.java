package masters.test2;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import masters.Constants;
import masters.Constants.State;
import masters.image.ImageDTO;
import masters.superpixel.SuperPixelDTO;
import masters.superpixel.SuperPixelHelper;
import masters.utils.DataHelper;
import masters.utils.ParametersContainer;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    
    public void testSuperpixelSegmentation() {
    	String path = "E:\\Studia\\CSIT\\praca_magisterska\\thesis\\tests\\peppers.png";
    	String savePath = "E:\\Studia\\CSIT\\praca_magisterska\\thesis\\tests\\peppers_sp_border_300_50.png";
    	String savePath3 = "E:\\Studia\\CSIT\\praca_magisterska\\thesis\\tests\\peppers_mean_300_50.png";
    	ParametersContainer parameterContainer = ParametersContainer.getInstance();
    	
    	Constants.NUMBER_OF_SUPERPIXELS = 300;
    	Constants.RIGIDNESS = 50;
    	
    	ImageDTO imageDTO = DataHelper.getSingleImageSegmented(new File(path), null, null, State.TEST, parameterContainer);
		
		DataHelper.saveImageSuperpixelBordersOnly(imageDTO, imageDTO.getSuperPixels(), savePath);
		DataHelper.saveImageSuperpixelsMeanColour(imageDTO, imageDTO.getSuperPixels(), savePath3);
		assertTrue(true);
    }
}
