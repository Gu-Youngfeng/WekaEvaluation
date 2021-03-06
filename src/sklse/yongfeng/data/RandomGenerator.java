package sklse.yongfeng.data;

import java.util.Random;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;

/***
 * <p>class <b>RandomGenerator</b> is used to proportional randomly select <b>SIZE</b> instances 
 * from original dataset for <b>N</b> times. Here is the example of generating N arff of codec.arff, </p>
 * <pre>
 * INPUT  >> files/codec.arff<br>
 * OUTPUT >> files/generated/codec1.arff, 
 *           files/generated/codec2.arff, ..., 
 *           files/generated/codecN.arff
 * </pre>
 * <p><b>* In our experiments, we use this class to randomly select 10 datasets from each projects.</b></p>
 */
public class RandomGenerator {
	
	/** generation times*/
	public static final int N = 10;
	
	/** sample size*/
	public static final int SIZE = 500;

	public static void main(String[] args) throws Exception {
		
		String[] paths = {"files/data/codec.arff",
				"files/data/ormlite.arff", "files/data/jsqlparser.arff", "files/data/collections.arff",
				"files/data/io.arff", "files/data/jsoup.arff", "files/data/mango.arff"};
		
		for(int i=0; i<paths.length; i++){
			generateProject(paths[i], N, SIZE); // generate one project
			System.out.printf("%-15s%s%d%s\n", filterName(paths[i]), " has already generated ", N, " arff files!");
		}
	}
	
	/***
	 * <p> To generate <b>genTimes</b> arff from the project in <b>path</b>, each arff has <b>sampleSize</b> crashes. 
	 * </p>
	 * @param path arff path
	 * @param times generation times
	 * @throws Exception
	 */
	public static void generateProject(String path, int genTimes, int sampleSize) throws Exception {
		
		for(int i=1; i<=genTimes; i++){
			generateARFF(path, i, sampleSize); // generate N sample according to different random seeds
		}		
		
	}
	
	/**<p>Generate Random sample according to random seed on Desktop, each sample has the same distribution of InTrace/OutTrace
	 *  and have <b>SIZE</b> instances.
	 * </p>
	 * @param path original arff file to be sampled in path
	 * @param rand random seed
	 * @param num the number of selection
	 * */
	public static void generateARFF(String path, int rand, int num) throws Exception{
		/*** original dataset reading */
		Instances data = DataSource.read(path);
		data.setClassIndex(data.numAttributes()-1);
		
		/*** randomize the dataset */
		data.randomize(new Random(rand));
		
		/*** dataIn to save instances of InTrace class */
		Instances dataIn = new Instances("dataIn", InsMerge.getStandAttrs(), 1);
		dataIn.setClassIndex(dataIn.numAttributes() - 1);

		/*** dataOut to save instances of OutTrace class */
		Instances dataOut = new Instances("dataOut", InsMerge.getStandAttrs(), 1);
		dataIn.setClassIndex(dataIn.numAttributes() - 1);
		
		/*** add OutTrace instances into dataOut */
		for(int i=0; i<data.numInstances(); i++){
			if(data.get(i).stringValue(data.get(i).classAttribute()).equals("OutTrace")){
				dataOut.add(data.get(i));
			}
		}
		
		/** add InTrace instances into dataIn */
		for(int i=0; i<data.numInstances(); i++){
			if(data.get(i).stringValue(data.get(i).classAttribute()).equals("InTrace")){
				dataIn.add(data.get(i));
			}
		}
		
		/*** get the In/Out ratio in original dataset */
		int inTrace = dataIn.numInstances();
		int outTrace = dataOut.numInstances();
		double ratioI = inTrace*1.0/(outTrace + inTrace);
		
		/*** expected number to select from original dataset*/
		int intrace = (int) (num * ratioI);
		int outtrace = num - intrace;
		
		/** create new generated dataset train*/
		Instances train = new Instances("dataIn", InsMerge.getStandAttrs(), 1);
		train.setClassIndex(train.numAttributes() - 1);
	
		/** train get X instances from dataIn*/
		for(int i=0; i<intrace; i++){
			train.add(dataIn.get(i));
		}
		
		/** train get Y instances from dataOut*/
		for(int j=0; j<outtrace; j++){
			train.add(dataOut.get(j));
		}
		
		/** save the dataset in path, we save the arff into D:/Users/LEE/Desktop/New_Data/XXX.arff */
		String filename = "files/generated/" + filterName(path) + rand + ".arff";
		DataSink.write(filename, train);

	}
	
	/**To get your file name from the path.*/
	public static String filterName(String path){
		String name = null;
		int indexXie = path.lastIndexOf("/");
		int indexDian = path.lastIndexOf(".");
		name = path.substring(indexXie+1, indexDian);
		return name;
	}

}
