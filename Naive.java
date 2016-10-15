import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Naive {

	List<String> featureVector = new ArrayList<String>();
	List<String> featureNameList = new ArrayList<String>();
	Map<String, List<String>> features = new HashMap<String, List<String>>();
	Map<String, Integer> labelCounts = new HashMap<String, Integer>();
	String attributeName;
	Map<String, Integer> featureCounts = new HashMap<String, Integer>();
	int aa = 0,ab=0,ba=0,bb=0;
	
	
	void readArff(String path) throws IOException {
		File n = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(n));
		try {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				if (!line.startsWith("@") && (!line.startsWith("%"))) {
					// alist.add(new ArrayList(line.toLowerCase().split(","));
					featureVector.add(line.toLowerCase());
				} else {
					if (!line.trim().toLowerCase().startsWith("@relation")
							&& (!(line.trim().toLowerCase().startsWith("@data")))) {
						featureNameList.add(line.trim().toLowerCase()
								.split(" ")[1]);
						if (line.contains("{")) {
							String[] featureValues = (line.substring(
									line.indexOf("{") + 1,
									line.lastIndexOf("}") - 1)).split(",");
							List<String> list = new ArrayList<String>(
									featureValues.length);
							for (String s : featureValues) {
								list.add(s.trim());
							}
							features.put(
									line.trim().toLowerCase().split(" ")[1],
									list);
							attributeName = line.trim().toLowerCase()
									.split(" ")[1];
						}
					}
				}
			}
		//	System.out.println(features.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			br.close();
		}
	}

	public void TrainClassifier() {
		initializeLabelCount();
		for (int i = 0; i < featureVector.size(); i++) {
			int a = (labelCounts
					.get(featureVector.get(i).split(",")[featureVector.get(i)
							.split(",").length - 1]));
			labelCounts.put(featureVector.get(i).split(",")[featureVector
					.get(i).split(",").length - 1], a + 1);
			for (int l = 0; l < featureVector.get(i).split(",").length - 1; l++) {
				if ((featureCounts
						.get(featureVector.get(i).split(",")[featureVector.get(
								i).split(",").length - 1]
								+ featureNameList.get(l)
								+ featureVector.get(i).split(",")[l])) != null) {
					int s = featureCounts
							.get(featureVector.get(i).split(",")[featureVector
									.get(i).split(",").length - 1]
									+ featureNameList.get(l)
									+ featureVector.get(i).split(",")[l]);
					featureCounts
							.put(featureVector.get(i).split(",")[featureVector
									.get(i).split(",").length - 1]
									+ featureNameList.get(l)
									+ featureVector.get(i).split(",")[l], s + 1);
				} else {
					featureCounts.put(
							featureVector.get(i).split(",")[featureVector
									.get(i).split(",").length - 1]
									+ featureNameList.get(l)
									+ featureVector.get(i).split(",")[l], 1);

				}

			}
		}

		Set<String> classValues = labelCounts.keySet();
		Iterator<String> iter = classValues.iterator();

		for (List<String> value : features.values()) {
			while (iter.hasNext()) {
				Object obj = iter.next();
				int aw = labelCounts.get(obj) + value.size();
				labelCounts.put(obj.toString(), aw);
			}
		}
	}

	public void TestClassifier(String path) throws IOException {
		File testfile = new File(path);
		
		BufferedReader brt = new BufferedReader(new FileReader(testfile));
		try {
			String line = brt.readLine();
			while (line!= null) {
				if(!line.startsWith("%") && !line.startsWith("@")){
				String data[] = line.trim().split(",");
				String classLabel = classify(data);
				if( data[data.length-1].equalsIgnoreCase("metastases") && classLabel.equalsIgnoreCase("metastases"))
				{
					aa++;
				}
				else if(data[data.length-1].equalsIgnoreCase("metastases") && classLabel.equalsIgnoreCase("malign_lymph") )
				{
					ab++;
				}
				else if(data[data.length-1].equalsIgnoreCase("malign_lymph") && classLabel.equalsIgnoreCase("metastases"))
				{
					ba++;
				}
				
				else if(data[data.length-1].equalsIgnoreCase("malign_lymph") && classLabel.equalsIgnoreCase("malign_lymph"))
				{
					bb++;
				}
				System.out.println("classifier :   "
						+ classify(data) + ": given :" + data[data.length - 1]);
				}
				line=brt.readLine();
			}
		} catch (Exception e) {

		}
		finally
		{
			brt.close();
		}
	}

	public String classify(String[] data) {
		Map<String, Double> probabilityPerLabel = new HashMap<String, Double>();
		Set<String> classValues = labelCounts.keySet();
		Object objlabel = null;
		Iterator iter = classValues.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			double logProb = 0.0;
			double tempValue = 0.0;
			for (int k = 0; k < data.length-1; k++){
				 tempValue = (double)featureCounts.getOrDefault((obj.toString()+featureNameList.get(k)+data[k]),1)
						/ labelCounts.get(obj);
			logProb += Math.log(tempValue);
			}
			double tempValue2 = (double)labelCounts.get(obj) / sum(labelCounts.values());
			probabilityPerLabel.put(
					obj.toString(),tempValue2* Math.exp(logProb));
		}
	 objlabel = probabilityPerLabel.get(Collections
				.max(probabilityPerLabel.values()));
	 String maxprobkey = null;
	 double maxProbKey = 0.0;
		Iterator it = probabilityPerLabel.entrySet().iterator();
		for (Map.Entry<String, Double> entry : probabilityPerLabel.entrySet()) {
			String key = entry.getKey();
			double value = entry.getValue().doubleValue();
			if(maxProbKey < value){
				maxProbKey = value;
				maxprobkey=key;
			}
			System.out.println(key + " " + value);
		}
		return maxprobkey;

	}

	public int sum(Collection<Integer> c) {
		int sumofValues = 0;
		Iterator itr = c.iterator();
		while (itr.hasNext())
			sumofValues += (Integer) itr.next();

		return sumofValues;
	}

	public void initializeLabelCount() {
		for (int i = 0; i < features.get(attributeName).size(); i++) {
			labelCounts
					.put(features.get(attributeName).get(i).toLowerCase(), 0);
		}
	}

	public void printConfusionMatrix()
	{
		System.out.println("\n \nConfusion Matrix == \n");
		System.out.println("a   b <-- classfied as");
		System.out.println(aa+"  "+ab+"  " +" | a= metastases");
		System.out.println(ba+"  "+bb+"  "+" | b=malign_lymph");
		
		System.out.println("Correctly classified instances:"+(aa+bb)+"   Correctness Percentage:"+((double)(aa+bb)/(aa+bb+ab+ba))*100+"%");
		System.out.println("Correctly classified instances:"+(ab+ba)+"   Correctness Percentage:"+((double)(ab+ba)/(aa+bb+ab+ba))*100+"%");

	}
	public static void main(String[] args) throws IOException {
		if(args.length!=2)
		{
			System.err.print("Less number of input arguments passed: training_file test_file needs to given");
			System.exit(1);
		}
		Naive nv = new Naive();
		nv.readArff(args[0]);
		nv.TrainClassifier();
		nv.TestClassifier(args[1]);
		nv.printConfusionMatrix();
	}
}
