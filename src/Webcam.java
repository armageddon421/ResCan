import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.util.Locale;

import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.EngineStateError;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.*;



public class Webcam {
	
	static Synthesizer synthesizer1;

    public static void main (String args[]){

    System.out.println("Hello, OpenCV");
    // Load the native library.
    System.loadLibrary("opencv_java248");

    VideoCapture camera = new VideoCapture();
    
    
    camera.open(0); //Useless
    if(!camera.isOpened()){
        System.out.println("Camera Error");
    }
    else{
        System.out.println("Camera OK?");
    }
    
    Voice kevinHQ = new Voice("kevin", 
    		Voice.GENDER_DONT_CARE, Voice.AGE_DONT_CARE, null);
    
    
    SynthesizerModeDesc generalDesc = new SynthesizerModeDesc(
    		null,          // engine name
                    "general",     // mode name
                    Locale.US,     // locale
                    null,          // running
                    null);         // voice
                
    	    try {
				synthesizer1 =
				        Central.createSynthesizer(generalDesc);
				
				synthesizer1.allocate();
				
				
			    try {
					synthesizer1.getSynthesizerProperties().setVoice(kevinHQ);
					synthesizer1.getSynthesizerProperties().setPitch(10);
					synthesizer1.getSynthesizerProperties().setPitchRange(0);
					
				} catch (PropertyVetoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    try {
					synthesizer1.resume();
				} catch (AudioException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (EngineStateError e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    synthesizer1.speakPlainText("Hello!", null);
				
				
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    
    while(true){
	    Mat frame = new Mat();
	    
	
	    //camera.grab();
	    //System.out.println("Frame Grabbed");
	    //camera.retrieve(frame);
	    //System.out.println("Frame Decoded");
	
	    camera.read(frame);
	    Core.flip(frame, frame, -1);
	
	    /* No difference
	    camera.release();
	    */
	
	    //System.out.println("Captured Frame Width " + frame.width());
	    
	    doMagic(frame);
	    showResult(toBufferedImage( frame));
	    
	    /*try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	    }
    }
    
    public static Image toBufferedImage(Mat m){
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() > 1 ) {
            Mat m2 = new Mat();
            m2 = m;
            //Imgproc.cvtColor(m,m2,Imgproc.COLOR_BGR2RGB);
            type = BufferedImage.TYPE_3BYTE_BGR;
            m = m2;
        }
        byte [] b = new byte[m.channels()*m.cols()*m.rows()];
        m.get(0,0,b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
        image.getRaster().setDataElements(0, 0, m.cols(),m.rows(), b);
        return image;

    }
    
    static JLabel outLabel;
    
    public static void showResult(Image img) {
       
    	if(outLabel==null){
	        JFrame frame = new JFrame();
	        outLabel = new JLabel(new ImageIcon(img));
	        frame.getContentPane().add(outLabel);
	        frame.pack();
	        frame.setVisible(true);
    	}
    	else{
    		outLabel.setIcon(new ImageIcon(img));
    	}

    }
    
    public static Mat doMagic(Mat image) {
       	
    	Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);
        //Mat small = image.submat(image.rows()/2-100, image.rows()/2+100,image.cols()/2-200,image.cols()/2+200);
    	//Mat small = image.submat(100,image.rows()-100,50,image.cols()-50);
        //Mat small = image;
    	Mat small = image.submat(50,image.rows()-50,50,image.cols()-50);
        
    	Scalar mean = Core.mean(small);

        //Scalar lastcolor = new Scalar(0,0,0,0);
        int nums[] = new int[small.cols()];
        double avgy[] = new double[small.cols()];
        //double colors[][] = new double[small.cols()][4];
        for(int x=0; x<small.cols(); x += 1){

            int num = 0;
            for(int y=0; y<small.rows(); y += 1){
                double res[] = small.get(y,x);
                if(res == null) continue;
                if(res[0] > mean.val[0]*0.5 && res[1] > mean.val[1]*0.5 && res[2] > mean.val[2]*0.5){
                    //small.put(y,x,0,0,0);
                }
                else{
                    num++;
                    avgy[x] += y;
                }
            }
            if(num>2){
                nums[x] = num;
                avgy[x] /= num;
            }
        }

        double avgnum = 0;
        int avgnumnum = 0;
        for(int x=0; x<small.cols(); x += 1){
            if(nums[x] > 0){
                avgnum += nums[x];
                avgnumnum++;
            }
        }
        int minx = -1;
        int maxx = 0;
        if(avgnumnum > 0){
            avgnum /= avgnumnum*1.0;

            for(int x=0; x<small.cols(); x += 1){
                if(nums[x] > avgnum+15){
                    if(minx < 0) minx = x;
                    maxx = x;

                }
            }
        }

        if(minx>=0){
            int miny = (int)avgy[minx];
            int maxy = (int)avgy[maxx];


            int minx2 = minx-20; if(minx2<0)minx2=0; if(minx2>avgy.length-1) minx2=avgy.length-1;
            int maxx2 = maxx+20; if(maxx2<0)maxx2=0; if(maxx2>avgy.length-1) maxx2=avgy.length-1;
            double a = (avgy[maxx2]-avgy[minx2])/((maxx2)-(minx2));

            miny = (int)(a*(minx-minx2)+avgy[minx2]);
            maxy = (int)(a*(maxx-maxx2)+avgy[maxx2]);

            //Mat tiny = small.submat(y1, y2, minx, maxx);

            int w = maxx-minx;

            if(w>10 && a < 0.7 && a > -0.7){

                for(int x=0;x<w;x++){
                    int y = (int)(a*x) + miny;
                    if(true){//y>40 && y<small.rows()-40){
                    	double a2 = -1.0/a;
                    	
                    	double res[] = new double[3];

                		int x2 = x+minx;
                		
                		int oy = 15;
                		int ox = (int)(oy/a2);
                		
                		double col1[] = small.get(y+oy, x2 + ox);
                		small.put(y+oy, x2+ox, 0.0,0.0,255.0);
                		
                		oy = -15;
                		ox = (int)(oy/a2);
                		
                		double col2[] = small.get(y+oy, x2 + ox);
                		small.put(y+oy, x2+ox, 0.0,0.0,255.0);
                		
                		oy = 25;
                		ox = (int)(oy/a2);
                		
                		double col3[] = small.get(y+oy, x2 + ox);
                		small.put(y+oy, x2+ox, 0.0,0.0,255.0);
                		
                		oy = -25;
                		ox = (int)(oy/a2);
                		
                		double col4[] = small.get(y+oy, x2 + ox);
                		small.put(y+oy, x2+ox, 0.0,0.0,255.0);
                		
                		if(col1 != null && col2 != null && col3 != null && col4 != null){
                    		res[0] = (col1[0] + col2[0] + col3[0] + col4[0])/4;
	                        res[1] = (col1[1] + col2[1] + col3[1] + col4[1])/4;
	                        res[2] = (col1[2] + col2[2] + col3[2] + col4[2])/4;
	                        image.put(0, x, res);
                		}
                	

                        
                    }
                }
                Mat res = new Mat();
                Imgproc.resize(image.submat(new Rect(0,0,w,1)),res, new Size(image.cols(),50),0,0, Imgproc.INTER_NEAREST);
                res.copyTo(image.submat(new Rect(0,0,res.cols(),res.rows())));
                
                detect(image,res);

            }

            Core.line(small,new Point(minx,miny), new Point(maxx, maxy), new Scalar(255,0,0),4);
            
        }
        
        return image;
    }

    static double codes[][] =  {{20,20,20}, //black
            {71,53,38}, //brown
            {105,31,40}, //red
            {160,90,50}, //orange
            {157,123,39}, //yellow
            {41,70,46}, //green
            {40,73,86}, //blue
            {75,55,75}, //violet
            {73,65,62}, //gray
            {200,200,200} //white
            };


    static double codesview[][] =  {{0,0,0}, //black
            {139,69,19}, //brown
            {255,0,0}, //red
            {255,128,0}, //orange
            {255,255,0}, //yellow
            {0,255,0}, //green
            {0,0,255}, //blue
            {200,0,255}, //violet
            {128,128,128}, //gray
            {255,255,255} //white
    };
    
    static String codename[] =  {"black", //black
        "brown", //brown
        "red", //red
        "orange", //orange
        "yellow", //yellow
        "green", //green
        "blue", //blue
        "violet", //violet
        "gray", //gray
        "white" //white
};


    private static double coldist(double c1[], double c2[]){
        return Math.sqrt(Math.pow(c1[0]-c2[0],2)
            + Math.pow(c1[1]-c2[1],2)
            + Math.pow(c1[2]-c2[2],2));
    }

    private static double coldist2(double c1[], double c2[]){
        return Math.pow(c1[0]-c2[0],2)
                + Math.pow(c1[1]-c2[1],2)
                + Math.pow(c1[2]-c2[2],2);
    }
    
    private static int contfound = 0;
    private static int lastresult[];
    
    private static void detect(Mat image, Mat res) {
        double bg[] = new double[3];

        for(int x=res.cols()-50;x<res.cols();x++){
            double val[] = res.get(0,x);
            for(int i=0;i<3;i++){
                bg[i] += val[i] / 50.0;
            }
        }

        double bgdists[] = new double[res.cols()];
        double avgdist = 0;
        for(int x=0;x<res.cols()-5;x++){
            bgdists[x] = coldist2(bg,res.get(0,x))/100;
            avgdist += bgdists[x]/res.cols();
            Core.line(image,new Point(x*image.cols()/res.cols(), bgdists[x]), new Point((x+1)*image.cols()/res.cols(), bgdists[x]), new Scalar(255,0,0));
        }
        Core.line(image,new Point(0, avgdist), new Point(image.cols(), avgdist), new Scalar(0,0,255));
        
        int coldet[] = new int[res.cols()];
        for(int x=0;x<res.cols();x++){
            if(bgdists[x] > avgdist*1.0){
                double col[] = res.get(0,x);
                double min = 100000;
                int minc = -1;
                for(int c=0;c<codes.length;c++){
                    double dist = coldist2(col, codes[c]);
                    if(dist<min) {
                        min = dist;
                        minc = c;
                    }
                }
                coldet[x] = minc;
                if(minc >= 0){
                    Core.rectangle(image, new Point(x*image.cols()/res.cols(),50), new Point((x+1)*image.cols()/res.cols(),100), new Scalar(codes[minc]), -1);
                    Core.rectangle(image, new Point(x*image.cols()/res.cols(),100), new Point((x+1)*image.cols()/res.cols(),150), new Scalar(codesview[minc]), -1);
                    
                }

            }
            else{
            	coldet[x] = -1;
            }
        }
        
        int numconti = 0;
        int numcodes = 0;
        int sumcodes[] = new int[res.cols()];
        //int sumc[] = new int[codes.length];
        int result[] = new int[3];
        boolean found = false;
        for(int x=0;x<res.cols();x++){
        	if(coldet[x] == -1 && numconti > 20){ //20 continuous at least
        		//System.out.println("contiend");
        		int sumc[] = new int[codes.length];
        		for(int i=0;i<numconti-20;i++){
        			sumc[sumcodes[i]]++;
        		}
        		
        		
        		int maxnum = 0;
        		int code = -1;
        		for(int i=0;i<codes.length;i++){
        			if(sumc[i] > maxnum){
        				maxnum = sumc[i];
        				code = i;
        			}
        			
        		}
        		if(code != -1){
        			
        			//darstellen
        			
        			Core.line(image, new Point(x-10,0), new Point(x-10,100), new Scalar(0,255,0));
        			Core.rectangle(image, new Point(numcodes*image.cols()/4,image.rows()-50), new Point((numcodes+1)*image.cols()/4,image.rows()), new Scalar(codesview[code]), -1);
        			Core.putText(image, codename[code], new Point(numcodes*image.cols()/4,image.rows()-10), Core.FONT_HERSHEY_TRIPLEX, 1.0, new Scalar(200,200,200));
        			//System.out.printf("found: %d\n", code);
        			result[numcodes] = code;
        			numcodes++;
        			if(numcodes >= 3){
        				found = true;
        				break;
        			}
        		}
        		numconti = 0;
        		
        		sumcodes = new int[res.cols()];
        	}
        	else if (coldet[x] >= 0){
        		if(numconti > 10) sumcodes[numconti-10] = coldet[x]; //don't obey the left border
        		if(numconti == 10) Core.line(image, new Point(x,0), new Point(x,100), new Scalar(0,255,0));
        		numconti++;
        		//System.out.printf("num: %d coldet: %d\n", numconti, coldet[x]);
        	}
        	else{
        		numconti = 0;
        	}
        	
        }
        
        
        
        if(found){
        	if(lastresult == null) lastresult = result;
        	if(result[0] == lastresult[0] && result[1] == lastresult[1] && result[2] == lastresult[2]){
	    		contfound++;
	    		if(contfound > 10){
	    			String speak = String.format("%s %s %s\n", codename[result[0]], codename[result[1]], codename[result[2]]);
	    			System.out.printf(speak);
	    			
	    			
	    			double resistance = calcvalue(result);
	    			String unit = " Ohm";
	    			if(resistance >= 1000.0){
	    				resistance /= 1000;
	    				unit = "k";
	    			}
	    			if(resistance >= 1000.0){
	    				resistance /= 1000;
	    				unit = "meg";
	    			}
	    			String valuestring = String.format("%.1f%s\n", resistance,unit);
	    			if(valuestring.split(",")[1].startsWith("0")){
	    				valuestring = String.format("%d%s\n", (int)resistance,unit);
	    			}
	    			System.out.println(valuestring);
	    			if((synthesizer1.getEngineState() & Synthesizer.QUEUE_EMPTY) >0){
	    				synthesizer1.speakPlainText(valuestring, null);
	    			}
	    			contfound = 0;
	    		}
        	}
        	else{
        		contfound -= 1;
        	}
    		
    		lastresult = result;
    	}
    	else{
    		contfound -= 2;
    		return;
    	}
        if(contfound < 0){
        	contfound = 0;
        }
        
    }
    
    static double calcvalue(int rings[]){
    	
    	double result = rings[0]*10 + rings[1];
    	result *= Math.pow(10,rings[2]);
    	
    	return result;
    }
}