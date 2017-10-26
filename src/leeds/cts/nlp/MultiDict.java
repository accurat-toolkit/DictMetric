package leeds.cts.nlp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.io.*;

import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.ling.CoreLabel;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.mit.jwi.morph.SimpleStemmer;
import java.net.URL;
import java.util.Hashtable;


class textTranslation implements Runnable{
	String input="";
	String transpath="";
	Map<String, String> map;
	private CountDownLatch threadsSignal; 
	public textTranslation(Map<String, String>map, String input, String transpath,CountDownLatch threadsSignal){
		this.map=map;
		this.input=input;
		this.transpath=transpath;
		this.threadsSignal=threadsSignal;
	}
	public void run(){
		try{
          String s="";
			String names[]=input.replaceAll("\\\\","/").split("/");	
			String fullname="";
			if (names[0].contains(":")){
				names[0]=names[0].replace(":", "@@@");
			}
			for (int k=0;k<names.length-1;k++){
				fullname=fullname+names[k]+"###";
			} 
			fullname=fullname+names[names.length-1]; 
			BufferedReader br2=new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));
	       BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(transpath+File.separator+fullname), "UTF8"));
           while (true){
          	 s=br2.readLine();
          	 if (s==null){
          		 break;
          	 }else{
          		 if (s.length()>0){
          			 String ss="";
                  	 Tokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(s), new CoreLabelTokenFactory(), "");
               		while (tokenizer.hasNext()) {
               		  CoreLabel token = tokenizer.next();
               		  String word=token.word();
               	      String tok=word.toLowerCase();
               	      if (tok.length()>1){
                 	   String candidate=map.get(tok);
               	      if (candidate==null){
               	    	  if (word.charAt(0)>='A' &&word.charAt(0)<='Z' &&tok.matches("[a-z-]+")==true){ // named entities
             				 ss=ss+word+" ";
                      		}
               	    	  }else{
               	    		  String str[]=candidate.split("\\ ");
               	    		   for (int i=0;i<str.length;i++){
                				 ss=ss+str[i]+" ";
               	    		   }
               	    	  }
          		 }
          	 }
               		if (ss.length()>0){
               			bw.write(ss);
               			bw.newLine();
               		}
           }
          	 }
           }
   		bw.flush();
   		bw.close();
   		br2.close();
   		threadsSignal.countDown();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}

class textStemming implements Runnable{
	String input="";
	String savepath="";
	WordnetStemmer stem;
	private CountDownLatch threadsSignal;
	public textStemming(String input, String savepath, WordnetStemmer stem,CountDownLatch threadsSignal){
		this.input=input;
		this.savepath=savepath;
		this.stem=stem;
		this.threadsSignal=threadsSignal;
	}
	public void BubbleSort(List x) {   
		  for (int i = 0; i < x.size(); i++) {   
		   for (int j = i + 1; j < x.size(); j++) {   		 
		    if (x.get(i).toString().length()>x.get(j).toString().length()){
		    	String temp=x.get(i).toString();
		    	x.set(i, x.get(j).toString());
		    	x.set(j, temp);
		    }
		   }   
		  }   
		 } 
	public void run(){
		try{	
			 File f5=new File(input);
			 String fullname=f5.getName();
			 if (!input.contains("###")){
				 fullname="";
				 String names[]=input.replaceAll("\\\\","/").split("/");
				    if (names[0].contains(":")){
				    	names[0]=names[0].replace(":", "@@@");
				    }
					for (int k=0;k<names.length-1;k++){
						fullname=fullname+names[k]+"###";
					}
					fullname=fullname+names[names.length-1];
			 }
   	    BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));
        BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(savepath+File.separator+fullname), "UTF8"));
        String s="";
        while (true){
       	 s=br.readLine();
       	 if (s==null){
       		 break;
       	 }else{
       		 if (s.length()>0){
       			 String ss="";
               	 Tokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(s), new CoreLabelTokenFactory(), "");
            		while (tokenizer.hasNext()) {
            		  CoreLabel token = tokenizer.next();
            		  String word=token.word();
            		 List alist=stem.findStems(word);
   				 if (alist.size()>0){
   				 if (alist.size()>1){
   					 BubbleSort(alist);
   				 }
   				 word=alist.get(0).toString();
   				 }
   				 ss=ss+word+" ";
            		}
            		if (ss.length()>0){
            			bw.write(ss);
            			bw.newLine();
            		}
       		 }
       	 }
        }
		bw.flush();
		bw.close();
		br.close();
		threadsSignal.countDown();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}



class textvectorization  implements Runnable {
	String input="";
	ArrayList stopword;
	String docname="";
	BufferedWriter bw;
	private CountDownLatch threadsSignal; 
//	ConcurrentLinkedQueue<String> queue;
	public textvectorization(ArrayList stopword, BufferedWriter bw, String input, String docname,CountDownLatch threadsSignal)  //index表示数组位置标号 
//	public textvectorization(ArrayList stopword, ConcurrentLinkedQueue<String> queue, String input, String docname,CountDownLatch threadsSignal)  //index表示数组位置标号 
	{
   	  this.input=input;
	  this.stopword=stopword;
	  this.docname=docname;
	//  this.queue=queue;
	  this.bw=bw;
	  this.threadsSignal=threadsSignal;
	 }
	public void  write(String s){
		try{
			synchronized(bw){
			bw.write(s);
			bw.newLine();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void run(){
	   try{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));
       ArrayList word=new ArrayList();
       ArrayList count=new ArrayList();
       String s="";
  	 while (true){
      	 s=br.readLine();
      	 if (s==null){
      		 break;
      	 }else{
      		 String t[]=s.split("\\ ");
      		 for (int j=0;j<t.length;j++){
      			 if (t[j].length()>2){	
      				 t[j]=t[j].toLowerCase();
      				 if (t[j].charAt(0)>='a' &&t[j].charAt(t[j].length()-1)<='z'){ //judge an English word
      					 if (!stopword.contains(t[j])){
      						   if (!word.contains(t[j])){
      							   word.add(t[j]);
      							   count.add(1);
      						 }else{
      							 int p=word.indexOf(t[j]);
      							 int num=(Integer)count.get(p)+1;
      							 count.set(p, num);
      						 }
      					 }
      				 }
      			 }
      		 }
      	 }
       }
  	 String ss=docname;    	
  	 for (int k=0;k<word.size();k++){        		
  		 ss=ss+"	"+word.get(k).toString()+" "+count.get(k).toString(); //use tab as separator between features,and space between word (or index) and weight             		
  	 }
  	 write(ss);
  //	 bw.write(ss);
  //	 bw.newLine();
  //	 queue.add(ss);
  	 br.close();
  	threadsSignal.countDown();
   }catch(Exception ex){
	   ex.printStackTrace();
   }
}
}
class cosine  implements Runnable {
//	ArrayList DocName;
//	ArrayList alist;
	Hashtable ht;
	String sn="";
	String tn="";
	BufferedWriter bw;
	double threshold=0;
	CountDownLatch threadSignal;
//	public cosine(BufferedWriter bw, ArrayList DocName, ArrayList alist,String sn,String tn,double threshold,CountDownLatch threadSignal){
	public cosine(BufferedWriter bw, Hashtable ht,String sn,String tn,double threshold,CountDownLatch threadSignal){
	//	this.DocName=DocName;
		this.sn=sn;
		this.tn=tn;
	//	this.alist=alist;
		this.ht=ht;
		this.bw=bw;
		this.threshold=threshold;
		this.threadSignal=threadSignal;
	}
	
	public void  write(String s){
		try{
			synchronized(bw){
			bw.write(s);
			bw.newLine();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	 public double GETcosine(ArrayList ID1, ArrayList Val1, ArrayList ID2, ArrayList Val2){
		  
			double sim=0;
			double sum1=0;
			for (int i=0;i<Val1.size();i++){
			    double v1=Double.parseDouble(Val1.get(i).toString());
			    sum1=sum1+v1*v1;
			}
			double sum2=0;
			for (int i=0;i<Val2.size();i++){
			    double v2=Double.parseDouble(Val2.get(i).toString());
			    sum2=sum2+v2*v2;
			}
			for (int i=0;i<ID1.size();i++){
			    if (ID2.contains(ID1.get(i).toString())){
				int p=ID2.indexOf(ID1.get(i).toString());
				double v1=Double.parseDouble(Val1.get(i).toString());
				double v2=Double.parseDouble(Val2.get(p).toString());
				sim=sim+v1*v2;
			    }
			}
			sim=sim/(Math.sqrt(sum1)*Math.sqrt(sum2));
			// System.out.println(sim);
			sim=Math.floor(sim*10000+0.5)/10000;
			//  System.out.println(sim);
			return sim;				 
		    }
	   public void run(){
		   try {
			   if (ht.containsKey(sn) &&ht.containsKey(tn)){
				   
		//   if (DocName.contains(sn)&&DocName.contains(tn)){ 
				//    System.out.println(sdoc[i].getName()+" "+tdoc[j].getName());
				//    int p=DocName.indexOf(sn);  
				//	int q=DocName.indexOf(tn); 
		       //     String s1=alist.get(p).toString();
				   String s1=ht.get(sn).toString();
					String t1[]=s1.split("\\	");
					ArrayList value1=new ArrayList();
					ArrayList id1=new ArrayList();
					for (int k=1;k<t1.length;k++){
					    String w[]=t1[k].split("\\ "); //space between word(or index) and weight
					    id1.add(w[0]);
					    value1.add(w[1]);
					}
				//	String s2=alist.get(q).toString();
					String s2=ht.get(tn).toString();
					String t2[]=s2.split("\\	");
					ArrayList id2=new ArrayList();
					ArrayList value2=new ArrayList();
					for (int k=1;k<t2.length;k++){
					    String w[]=t2[k].split("\\ "); //space between word(or index) and weight
					    id2.add(w[0]);
					    value2.add(w[1]);
					}
					double sim=GETcosine(id1,value1,id2,value2);
					String snames[]=sn.split("###");
					if (snames[0].contains("@@@")){
						snames[0]=snames[0].replace("@@@", ":");
					}
					String sname=snames[0];
					for (int k=1;k<snames.length;k++){
						sname=sname+File.separator+snames[k];
					}
					String tnames[]=tn.split("###");
					if (tnames[0].contains("@@@")){
						tnames[0]=tnames[0].replace("@@@", ":");
					}
					String tname=tnames[0];
					for (int k=1;k<tnames.length;k++){
						tname=tname+File.separator+tnames[k];
					}
					String s=sname+"	"+tname+"	"+sim; 
					if (sim>=threshold){
				//	bw.write(s);
				//	bw.newLine();
					write(s);
					}
				    }
			      // else{
				    //	System.out.println(sn+" "+tn);
				   // }
		   threadSignal.countDown();
	   }catch(Exception ex){
			   ex.printStackTrace();
		   }
	   }
	}


class  nonENSourceVector implements Runnable {
	String input="";
	ArrayList stopword;
	String docname="";
	BufferedWriter bw;
	private CountDownLatch threadsSignal; 
//	ConcurrentLinkedQueue<String> queue;
	public nonENSourceVector(ArrayList stopword, BufferedWriter bw, String input, String docname,CountDownLatch threadsSignal)  //index表示数组位置标号 
//	public textvectorization(ArrayList stopword, ConcurrentLinkedQueue<String> queue, String input, String docname,CountDownLatch threadsSignal)  //index表示数组位置标号 
	{
   	  this.input=input;
	  this.stopword=stopword;
	  this.docname=docname;
	//  this.queue=queue;
	  this.bw=bw;
	  this.threadsSignal=threadsSignal;
	 }
	public void  write(String s){
		try{
			synchronized(bw){
			bw.write(s);
			bw.newLine();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void run(){
	   try{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));
       ArrayList word=new ArrayList();
       ArrayList count=new ArrayList();
       String s="";
  	 while (true){
      	 s=br.readLine();
      	 if (s==null){
      		 break;
      	 }else{
      		 String t[]=s.split("\\ ");
      		 for (int j=0;j<t.length;j++){
      			 if (t[j].length()>2){	
      				 t[j]=t[j].toLowerCase();
      		//		 if (t[j].charAt(0)>='a' &&t[j].charAt(t[j].length()-1)<='z'){ //judge an English word
      					 if (!stopword.contains(t[j])){
      						   if (!word.contains(t[j])){
      							   word.add(t[j]);
      							   count.add(1);
      						 }else{
      							 int p=word.indexOf(t[j]);
      							 int num=(Integer)count.get(p)+1;
      							 count.set(p, num);
      						 }
      					 }
      			//	 }
      			 }
      		 }
      	 }
       }
  	 String ss=docname;    	
  	 for (int k=0;k<word.size();k++){        		
  		 ss=ss+"	"+word.get(k).toString()+" "+count.get(k).toString(); //use tab as separator between features,and space between word (or index) and weight             		
  	 }
  	 write(ss);
  //	 bw.write(ss);
  //	 bw.newLine();
  //	 queue.add(ss);
  	 br.close();
  	threadsSignal.countDown();
   }catch(Exception ex){
	   ex.printStackTrace();
   }
}
}



class nonENTargetVector implements Runnable{
	String input="";
	ArrayList stopword;
	BufferedWriter bw;
	private CountDownLatch threadsSignal; 
	public nonENTargetVector(ArrayList stopword, BufferedWriter bw,String input,CountDownLatch threadsSignal){
		this.input=input;
		this.stopword=stopword;
		this.bw=bw;
		this.threadsSignal=threadsSignal;
	}
	public void  write(String s){
		try{
			synchronized(bw){
			bw.write(s);
			bw.newLine();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void run(){
		try{
			String names[]=input.replaceAll("\\\\","/").split("/");	
			String fullname="";
			if (names[0].contains(":")){
				names[0]=names[0].replace(":", "@@@");
			}
			for (int k=0;k<names.length-1;k++){
				fullname=fullname+names[k]+"###";
			} 
			fullname=fullname+names[names.length-1]; 
			
			BufferedReader br2=new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));
			String s=""; 
			ArrayList word=new ArrayList();
          ArrayList count=new ArrayList();
			while (true){
       	 s=br2.readLine();
       	 if (s==null){
       		 break;
       	 }else{
       		 if (s.length()>0){
               	 Tokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(s), new CoreLabelTokenFactory(), "");
            		while (tokenizer.hasNext()) {
            		  CoreLabel token = tokenizer.next();
            		  String tok=token.word().toLowerCase();
            		  String tokens[]=tok.split("\\ "); //<a href...> is a token in the standford tokenizer, but contains space
            		  for (int i=0;i<tokens.length;i++){	  
            		  if (tokens[i].length()>2 &&!tokens[i].startsWith("-")){
            				 if (!stopword.contains(tokens[i])){
      						   if (!word.contains(tokens[i])){
      							   word.add(tokens[i]);
      							   count.add(1);
      						 }else{
      							 int p=word.indexOf(tokens[i]);
      							 int number=(Integer)count.get(p)+1;
      							 count.set(p, number);
      						 }
      					 }
            		   }
            		}
            		}
       		 }
       	 }
			}
	String ss=fullname;
	 for (int k=0;k<word.size();k++){
		 ss=ss+"	"+word.get(k).toString()+" "+count.get(k).toString(); //use tab as separator between features,and space between word (or index) and weight
	 }
	 write(ss);
	 br2.close();
	 threadsSignal.countDown(); 
	}catch(Exception ex){
		ex.printStackTrace();
	}
	}
}

class nonENCosine implements Runnable{
	Hashtable ht;
	BufferedWriter bw;
	String sn="";
	String tn="";
	double threshold=0;
	private CountDownLatch threadsSignal;
   public nonENCosine(Hashtable ht, BufferedWriter bw, String sn, String tn, double threshold,CountDownLatch threadsSignal){
	   this.ht=ht;
	   this.bw=bw;
	   this.sn=sn;
	   this.tn=tn;
	   this.threshold=threshold;
	   this.threadsSignal=threadsSignal;
   }
   
   public void  write(String s){
		try{
			synchronized(bw){
			bw.write(s);
			bw.newLine();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	 public double GETcosine(ArrayList ID1, ArrayList Val1, ArrayList ID2, ArrayList Val2){
		  
			double sim=0;
			double sum1=0;
			for (int i=0;i<Val1.size();i++){
			    double v1=Double.parseDouble(Val1.get(i).toString());
			    sum1=sum1+v1*v1;
			}
			double sum2=0;
			for (int i=0;i<Val2.size();i++){
			    double v2=Double.parseDouble(Val2.get(i).toString());
			    sum2=sum2+v2*v2;
			}
			for (int i=0;i<ID1.size();i++){
			    if (ID2.contains(ID1.get(i).toString())){
				int p=ID2.indexOf(ID1.get(i).toString());
				double v1=Double.parseDouble(Val1.get(i).toString());
				double v2=Double.parseDouble(Val2.get(p).toString());
				sim=sim+v1*v2;
			    }
			}
			sim=sim/(Math.sqrt(sum1)*Math.sqrt(sum2));
			// System.out.println(sim);
			sim=Math.floor(sim*10000+0.5)/10000;
			//  System.out.println(sim);
			return sim;				 
		    }
	public void run(){
		try{
			   if (ht.containsKey(sn)&&ht.containsKey(tn)){ 
					//    System.out.println(sdoc[i].getName()+" "+tdoc[j].getName());
					   
			            String s1=ht.get(sn).toString();
						String t1[]=s1.split("\\	");
						ArrayList value1=new ArrayList();
						ArrayList id1=new ArrayList();
						for (int k=1;k<t1.length;k++){
						    String w[]=t1[k].split("\\ "); //space between word(or index) and weight
						    id1.add(w[0]);
						    value1.add(w[1]);
						}
						String s2=ht.get(tn).toString();
						String t2[]=s2.split("\\	");
						ArrayList id2=new ArrayList();
						ArrayList value2=new ArrayList();
						for (int k=1;k<t2.length;k++){
						    String w[]=t2[k].split("\\ "); //space between word(or index) and weight
						    id2.add(w[0]);
						    value2.add(w[1]);
						}
						double sim=GETcosine(id1,value1,id2,value2);
						String snames[]=sn.split("###");
						if (snames[0].contains("@@@")){
							snames[0]=snames[0].replace("@@@", ":");
						}
						String sname=snames[0];
						for (int k=1;k<snames.length;k++){
							sname=sname+File.separator+snames[k];
						}
						String tnames[]=tn.split("###");
						if (tnames[0].contains("@@@")){
							tnames[0]=tnames[0].replace("@@@", ":");
						}
						String tname=tnames[0];
						for (int k=1;k<tnames.length;k++){
							tname=tname+File.separator+tnames[k];
						}
						String s=sname+"	"+tname+"	"+sim; 
						if (sim>=threshold){
						write(s);
						}
					    }
			   threadsSignal.countDown();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}


public class MultiDict {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
             MultiDict a=new MultiDict();
       //        System.out.println("LETS GOO");
       //     a.translation("croatian", "english", "/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/HR-EN/dicmetric/hr.txt", "/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/HR-EN/dicmetric/temp");
      //       a.stemmer("english", "/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/HR-EN/dicmetric/en.txt", "/home/fzsu/WordNet-3.0", "/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/HR-EN/dicmetric/temp");
        //           a.nonENtext2vectors("romanian","german", "/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/RO-DE/dicmetric/de.txt", "/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/RO-DE/dicmetric/tmp");
         //      a.SelectedCOSINESimilarityWithoutDocPair("/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/HR-EN/dicmetric/temp", "/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/HR-EN/dicmetric/temp/e-stem", "/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/HR-EN/dicmetric/temp/croatian-stem", "/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/HR-EN/dicmetric/temp/result", 0);
         //   a.newtext2vectors("english", "croatian", "/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/HR-EN/dicmetric/temp");
         //    a.nonENCOSINESimilarityWithoutDocPair("/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/RO-DE/dicmetric/tmp", "/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/RO-DE/dicmetric/ro.txt", "/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/RO-DE/dicmetric/de.txt", "/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/RO-DE/dicmetric/tmp/output", 0);
          //      a.compare();
            
		
               if (args.length!=18){
        			System.out.println("Usage: java -jar DictMetric.jar --source SourceLanguage --target TargetLanguage --WN Path2WordNet --threshold value --input path2SourceFileList --input path2TargetFileList --output path2result --tempDir path2TemporaryDirectory --option [0|1]");
        			System.out.println("--source SourceLanguage: Language to be translate from");
        			System.out.println("--target TargetLanguage: Language to be translated to");
        			System.out.println("--WN path2WordNet: the full path to the WordNet installation directory");
        			System.out.println("--threshold value: output the document pairs with a comparability score >= threshold");
        			System.out.println("--input path2SourceFileList: path to the file that lists the full path to the documents in source language");
        			System.out.println("--input path2TargetFileList: path to the file that lists the full path to the documents in target language");
        			System.out.println("--output path2result: path to the file that store comparable document pairs with comparability scores");
        			System.out.println("--tempDir path2TemporaryDirectory: specify a path to a temporary directory (must exist) for storing intermediate outputs ");
        			System.out.println("--option [0|1]: translate the non-English text into English (1) or not (0), applied to non-English language pairs");
                }else{
               	String SL=args[1].toLowerCase();;
        			String TL=args[3].toLowerCase();
        			String WN=args[5];
        			double threshold=Double.parseDouble(args[7]);
        			String SP=args[9];
        			String TP=args[11];
        			String result=args[13];
        			String tempDir=args[15];
        			String option=args[17];
        			if (TL.equals("english")){
        				a.translation(SL, "english", SP, tempDir);
        				a.stemmer(SL, tempDir+File.separator+SL+"-translation.txt", WN, tempDir);
        				a.stemmer(TL,TP,WN,tempDir);
        				a.newtext2vectors(SL, TL, tempDir);
        				a.SelectedCOSINESimilarityWithoutDocPair(tempDir, tempDir+File.separator+SL+"-stem", tempDir+File.separator+TL+"-stem", result, threshold);
        			}else{
        			  if (option.equals("0")){
        				 a.translation(SL, TL, SP, tempDir);
        				a.nonENtext2vectors(SL, TL, TP, tempDir);
        				a.nonENCOSINESimilarityWithoutDocPair(tempDir, SP, TP, result, threshold);
        			  }else{
        				  a.translation(SL, "english", SP, tempDir);
        				  a.translation(TL, "english", TP, tempDir);
        				  a.stemmer(SL, tempDir+File.separator+SL+"-translation.txt", WN, tempDir);
        				  a.stemmer(TL, tempDir+File.separator+TL+"-translation.txt", WN, tempDir);
        				  a.newtext2vectors(SL, TL, tempDir);
        				  a.SelectedCOSINESimilarityWithoutDocPair(tempDir, tempDir+File.separator+SL+"-stem", tempDir+File.separator+TL+"-stem", result, threshold);
        			  }
        			}
        			}  
	}



	





/*
 * source: source language
 * target: target language
 * sourcepath: path to the file that lists the full path to the documents in source language
 * targetpath: path to the directory that stores translated texts.
 */
public void translation(String source, String target, String sourcepath, String targetpath){
	try{
		 long startTime=System.currentTimeMillis();
		System.out.println("Start translation...");
		Map<String, String> language = new HashMap<String, String>();
		language.put("english", "en");
		language.put("german", "de");
		language.put("croatian", "hr");
		language.put("greek", "el");
		language.put("estonian", "et");
		language.put("lithuanian", "lt");
		language.put("latvian", "lv");
		language.put("romanian", "ro");
		language.put("slovenian", "sl");
		String f=language.get(source.toLowerCase());
		String e=language.get(target.toLowerCase());
		File file = new File("."); 
	   String CurrentPath = file.getCanonicalPath();
	//   String dicpath=CurrentPath+File.separator+"dict/newdic"+File.separator+f+"_"+e+".txt"; 
	   String dicpath=CurrentPath+File.separator+"dict"+File.separator+f+"_"+e+".txt";  
		 String transpath=targetpath+File.separator+source+"-translation";
			File f5=new File(transpath);
			if (!f5.exists()){
				f5.mkdir();
			}
		 
		Map<String, String> map = new HashMap<String, String>();
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(dicpath), "UTF8"));
	//	   BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/media/FreeAgent Drive/dictionary/de_en.txt"), "UTF8"));
	    String s="";
	    while (true){
	    	s=br.readLine();
	    	if (s==null){
	    		break;
	    	}else{
	    		String t[]=s.split("\\  ");
	    		String w[]=t[0].split("\\ ");
	    		w[0]=w[0].toLowerCase();
	    		if (t.length==1){
	    			map.put(w[0],w[1]);
	    		}else{
	    			String u[]=t[1].split("\\ ");
	    			Double v1=Double.parseDouble(w[2]);
	    			Double v2=Double.parseDouble(u[2]);
	    			if (v1>=0.3 &&v2<0.1){
	    				map.put(w[0], w[1].trim()); //only keep one translation candidate
	    			}else{
	    				String ss=w[1]+" "+u[1];
	    				map.put(w[0], ss);
	    			}
	    		}	
	    	}
	    }
	    br.close();
	//    System.out.println(map.get("lietderīgs"));
	    int count=0;
	    BufferedReader br4=new BufferedReader(new FileReader(sourcepath));
	    while (true){
	    	s=br4.readLine();
	    	if (s==null){
	    		break;
	    	}else{
	    		count++;
	    	}
	    }
	    br4.close();
	    CountDownLatch threadSignal = new CountDownLatch(count);//初始化countDown
	    ExecutorService threadExecutor = Executors.newFixedThreadPool(10);
	    BufferedReader br1=new BufferedReader(new FileReader(sourcepath)); //path to the input file which lists the full path to the documents to be translated
		while (true){
			s=br1.readLine();
			if (s==null){
				break;
			}else{	
		      textTranslation tt=new textTranslation(map,s,transpath,threadSignal);
			   threadExecutor.submit(tt);
			}
		}
		threadSignal.await();
		threadExecutor.shutdown();
		 br1.close();
		 File ff=new File(transpath);
		 File[] files=ff.listFiles();
		 BufferedWriter bw1=new BufferedWriter(new FileWriter(targetpath+File.separator+source+"-translation.txt"));
		for (int i=0;i<files.length;i++){
			bw1.write(files[i].getAbsolutePath());
			bw1.newLine();
		}
		bw1.flush();
		bw1.close();
		System.out.println("translation is all done!");
		long  endTime=System.currentTimeMillis();
		   System.out.println("text translation processing time: "+(endTime-startTime)+" milliseconds");
			}catch(Exception ex){
		ex.printStackTrace();
	}			
}




private static void BubbleSort(List x) {   
	  for (int i = 0; i < x.size(); i++) {   
	   for (int j = i + 1; j < x.size(); j++) {   		 
	    if (x.get(i).toString().length()>x.get(j).toString().length()){
	    	String temp=x.get(i).toString();
	    	x.set(i, x.get(j).toString());
	    	x.set(j, temp);
	    }
	   }   
	  }   
	 } 


/*
 *  
 *  use stanford tokenizer (in Stanford POS tagger API) for word tokenization, and MIT JWI (the MIT Java Wordnet Interface) API for 
 * word-based stemming. 
 *  language: the original language to be stemmed.
 *  path: the path to the directory which contains texts to be stemmed;
 *  WNHome: the path to WordNet installation directory
 *  targetpath: the path to the directory which stores stemmed texts. 
 * 
 */
public void stemmer(String language, String path, String WNHome, String targetpath){
	try{
		System.out.println("start tokenization and lemmatization:");
	//	 String wnhome="/usr/local/WordNet-3.0";
		 String wnpath = WNHome + File.separator + "dict";
		 URL url = new URL("file", null, wnpath);
		  System.out.println(wnpath);
		 // construct the dictionary object and open it
		 IDictionary dict = new Dictionary(url);
		 dict.open();
		 WordnetStemmer stem=new WordnetStemmer(dict);
		 String savepath=targetpath+File.separator+language+"-stem";
		 File dir=new File(savepath); 
		 dir.mkdirs();
		 int count=0;
		 String s="";
		/* BufferedReader br4=new BufferedReader(new FileReader(path));
		 while (true){
			 s=br4.readLine();
			 if (s==null){
				 break;
			 }else{
				 count++;
			 }
		 }
		 br4.close();*/
	//	 long startTime=System.currentTimeMillis();
		// CountDownLatch threadSignal = new CountDownLatch(count);
		// ExecutorService threadExecutor = Executors.newFixedThreadPool(10);
		 BufferedReader br1=new BufferedReader(new FileReader(path));
		 while (true){
			 s=br1.readLine();
			 if (s==null){
				 break;
			 }else{
		      //    textStemming ts=new textStemming(s, savepath, stem,threadSignal);
			   //    threadExecutor.submit(ts);
					count++;
					 System.out.println(count+":"+s);
					 File f5=new File(s);
					 String fullname=f5.getName();
					 if (!s.contains("###")){
						 fullname="";
						 String names[]=s.replaceAll("\\\\","/").split("/");
					/*	 for (int k=0;k<names.length;k++){
							 System.out.println(k+" "+names[k]);
						 } */
					//	 System.out.println(names[0].contains(":"));
						    if (names[0].contains(":")){
						    	names[0]=names[0].replace(":", "@@@");
						    }
					
							for (int k=0;k<names.length-1;k++){
								fullname=fullname+names[k]+"###";
							}
							fullname=fullname+names[names.length-1];
					 }
	        	  BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(s), "UTF8"));
	             BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(savepath+File.separator+fullname), "UTF8"));
	             while (true){
	            	 s=br.readLine();
	            	 if (s==null){
	            		 break;
	            	 }else{
	            		 if (s.length()>0){
	            			 String ss="";
	                    	 Tokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(s), new CoreLabelTokenFactory(), "");
	                 		while (tokenizer.hasNext()) {
	                 		  CoreLabel token = tokenizer.next();
	                 		  String word=token.word();
	                 		 List alist=stem.findStems(word);
	        				 if (alist.size()>0){
	        				 if (alist.size()>1){
	        					 BubbleSort(alist);
	        				 }
	        				 word=alist.get(0).toString();
	        				 }
	        				 ss=ss+word+" ";
	                 		}
	                 		if (ss.length()>0){
	                 			bw.write(ss);
	                 			bw.newLine();
	                 		}
	            		 }
	            	 }
	             }
	     		bw.flush();
	     		bw.close();
	     		br.close();
			 }	 
         }
    //     threadSignal.await();
    //     threadExecutor.shutdown();
    //     long  endTime=System.currentTimeMillis();
		//  System.out.println("耗时:"+(endTime-startTime)+"毫秒");
     //    System.out.println("processing time of text stemming: "+(endTime-startTime)+" milliseconds");
		 System.out.println("tokenization and lemamtaization are done!");
	}catch(Exception ex){
		ex.printStackTrace();
	}			
}	


/*
 * stop word filtering, and convert the document texts into vectors.
 *  source: source language
 *  target: target language
 *  targetpath: path to the directory that store the index vectors.
 */
	 public void newtext2vectors(String source, String target, String targetpath){
		 
		 try{
			 System.out.println("start stopword filtering, and converting text into vector:");
			 File file = new File("."); 
			 String CurrentPath = file.getCanonicalPath();
			 String path=CurrentPath+File.separator+"stopwords"+File.separator+"stopwords_en.txt"; 
			 BufferedReader br1=new BufferedReader(new FileReader(path));
			 String s="";
			 ArrayList stopword=new ArrayList();
			 while (true){
				 s=br1.readLine();
				 if (s==null){
					 break;
				 }else{
					 stopword.add(s);
				 }
			 }
		//	 long startTime=System.currentTimeMillis();
		//	 ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
			 ExecutorService threadExecutor = Executors.newFixedThreadPool(10);
		//	 bw1=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetpath+File.separator+"abc.vectors"), "UTF8"));
             BufferedWriter bw1=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetpath+File.separator+"index.vectors"), "UTF8"));
             File f=new File(targetpath+File.separator+source+"-stem");
             File[] list=f.listFiles();
             CountDownLatch threadSignal = new CountDownLatch(list.length);//初始化countDown
             for (int i=0;i<list.length;i++){
            	 System.out.println(i+": "+list[i].getName());
            	 String input=list[i].getAbsolutePath();
            	 String docname=list[i].getName();
            	 textvectorization tv=new textvectorization(stopword,bw1,input,docname,threadSignal);
           // 	 textvectorization tv=new textvectorization(stopword,queue,input,docname,threadSignal);
           	 threadExecutor.submit(tv);
            	
           // 	 Thread t=new Thread(tv);
           //      t.start();
            	 
             }   
             threadSignal.await();//等待所有子线程执行完 
          /*     while (!queue.isEmpty()){
            	   bw1.write(queue.poll().toString());
            	   bw1.newLine();
               }  */
             f=new File(targetpath+File.separator+target+"-stem");
             list=f.listFiles();
             threadSignal = new CountDownLatch(list.length);//初始化countDown
             for (int i=0;i<list.length;i++){
            	 System.out.println(i+": "+list[i].getName());
            	String input=list[i].getAbsolutePath();
            	String docname=list[i].getName();
            	textvectorization tv=new textvectorization(stopword,bw1,input,docname,threadSignal);
           // 	textvectorization tv=new textvectorization(stopword,queue,input,docname,threadSignal);
            //	   Thread t=new Thread(tv);
           //    t.start();
            		 threadExecutor.submit(tv);
            	

             }  
             
             threadSignal.await();//等待所有子线程执行完 
            /* while (!queue.isEmpty()){
          	   bw1.write(queue.poll().toString());
          	   bw1.newLine();
             }  */
              bw1.flush();
              bw1.close();
			   threadExecutor.shutdown();
		//	   System.out.println(threadExecutor.isTerminated());
			//   if (threadExecutor.isTerminated()){
			 //  while(!threadExecutor.isTerminated());  
		//	   long  endTime=System.currentTimeMillis();
			//   System.out.println("耗时:"+(endTime-startTime)+"毫秒");
		//	   System.out.println("processing time of text vectorization: "+(endTime-startTime)+" milliseconds");
			   //  }
             System.out.println("The conversion of the document text into feature vectors is done!");
             
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
	 }	 

/*
 * This is for non-english language pair, and the translation is also not based on English
 * so word stemming will be skipped, and stopword filtering and text to vector conversion will be executed right after translation.	 
 * source: source language
 * target: target language
 * path: path to the file which lists the full path to the documents in target language
 * targetpath: path to the directory to store the index vectors.
 */

	 public void nonENtext2vectors(String source, String target, String path, String targetpath){
		 try{
			 
			 System.out.println("start stopword filtering, and converting text into vector:");
			 Map<String, String> language = new HashMap<String, String>();
				language.put("english", "en");
				language.put("german", "de");
				language.put("croatian", "hr");
				language.put("greek", "el");
				language.put("estonian", "et");
				language.put("lithuanian", "lt");
				language.put("latvian", "lv");
				language.put("romanian", "ro");
				language.put("slovenian", "sl");
				String e=language.get(target.toLowerCase());
			 File file = new File("."); 
			 String CurrentPath = file.getCanonicalPath();
			 String stoppath=CurrentPath+File.separator+"stopwords"+File.separator+"stopwords_"+e+".txt"; 
			 File stopFile=new File(stoppath);
			 ArrayList stopword=new ArrayList();
			 String s="";
			 if (stopFile.exists()){
			 BufferedReader br1=new BufferedReader(new FileReader(stoppath));
			 while (true){
				 s=br1.readLine();
				 if (s==null){
					 break;
				 }else{
					 stopword.add(s);
				 }
			 }
			 br1.close();
		 }
		//	 System.out.println("stopword= "+stopword.size()+stopword.get(stopword.size()-1).toString());
		//	 long startTime=System.currentTimeMillis();
			 BufferedWriter bw1=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetpath+File.separator+"index.vectors"), "UTF8"));
             File f=new File(targetpath+File.separator+source+"-translation");
             File[] list=f.listFiles();
             ExecutorService threadExecutor = Executors.newFixedThreadPool(10);
             CountDownLatch threadSignal = new CountDownLatch(list.length);//初始化countDown
             for (int i=0;i<list.length;i++){
            	 System.out.println(i+": "+list[i].getName());
            	 String input=list[i].getAbsolutePath();
            	 String docname=list[i].getName();
            	 nonENSourceVector tv=new nonENSourceVector(stopword,bw1,input,docname,threadSignal);
           	 threadExecutor.submit(tv);           	 
             }   
             threadSignal.await();//等待所有子线程执行完  
             
             BufferedReader br5=new BufferedReader(new FileReader(path)); //count the number of documents so that count down for thread numbers can be fixed.
             int num=0;
             while (true){
            	 s=br5.readLine();
            	 if (s==null){
            		 break;
            	 }else{
            		 num++;
            	 }
             }
           BufferedReader br1=new BufferedReader(new FileReader(path)); //path to the input file which lists the full path to the documents to be translated
           threadSignal = new CountDownLatch(num);//初始化countDown
     		while (true){
     			s=br1.readLine();
     			if (s==null){
     				break;
     			}else{	
     				nonENTargetVector tv=new nonENTargetVector(stopword,bw1,s,threadSignal);
     				threadExecutor.submit(tv);
     			}
     		}
     		threadSignal.await(); 
     		threadExecutor.shutdown();
             bw1.flush();
             bw1.close();
       //      long  endTime=System.currentTimeMillis();
			//   System.out.println("耗时:"+(endTime-startTime)+"毫秒");
       //      System.out.println("processing time of text vectorization: "+(endTime-startTime)+" milliseconds");
             System.out.println("The conversion of the document text into feature vectors is done!");
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
	 }	 
	 

	 /*
	 *  given a list of comparable document pairs, compute their comparability score by cosine measure 
	 *  targetpath: path to the directory that contains index vectors.
	 *  spath: path to the directory that contains the stemmed texts in source languages
	 *  tpath: path to the directory that contains the stemmed texts in target languages
	 *  outputpath: path to the file that store the comparability results
	 *  threshold: only output document pairs with comparability score>threshold.  
	 */
		 
		 public void SelectedCOSINESimilarityWithoutDocPair(String targetpath, String spath, String tpath, String outputpath, double threshold){
				try{
					System.out.println("start computing comparability score via cosine measure:");
					 BufferedReader br=new BufferedReader(new FileReader(targetpath+File.separator+"index.vectors"));                                            
				     BufferedWriter bw=new BufferedWriter(new FileWriter(outputpath));             
					String s="";
				//    ArrayList alist=new ArrayList();
				//    ArrayList DocName=new ArrayList();
					Hashtable ht=new Hashtable();
				    while (true){
					s=br.readLine();
					if (s==null){
					    break;
					}else{
				//	    alist.add(s);
					    String t[]=s.split("\\	"); //use tab   
				//	    DocName.add(t[0]);
					    ht.put(t[0], s);
					 }
				    }
				    br.close();
				    File file=new File(spath);
				    File sdoc[]=file.listFiles();
				    file=new File(tpath);
				    File tdoc[]=file.listFiles();
			//	    long startTime=System.currentTimeMillis();
				    ExecutorService threadExecutor = Executors.newFixedThreadPool(10);
				//    CountDownLatch threadSignal = new CountDownLatch(sdoc.length*tdoc.length);//初始化countDown
				   
				    for (int i=0;i<sdoc.length;i++){
				    	 CountDownLatch threadSignal = new CountDownLatch(tdoc.length);//初始化countDown
				    	for (int j=0;j<tdoc.length;j++){
				    	 String sn=sdoc[i].getName();
					    String tn=tdoc[j].getName();
					//    cosine cos=new cosine(bw, DocName, alist,sn,tn,threshold,threadSignal);
					    cosine cos=new cosine(bw, ht,sn,tn,threshold,threadSignal);
					//    Thread thread=new Thread(cos);
					    threadExecutor.submit(cos);
					//    cos=null;
				    	}
				    	threadSignal.await();
				    	bw.flush();
				    	System.out.println((i+1)*tdoc.length +" document pairs is done!");
				    }
				 //   threadSignal.await();
				//    bw.flush();
				    bw.close();
				    threadExecutor.shutdown();
		     //      long  endTime=System.currentTimeMillis();
				//    System.out.println("耗时:"+(endTime-startTime)+"毫秒");
		    //       System.out.println("processing time of computing comparability scores: "+(endTime-startTime)+"milliseconds");
		           System.out.println("The computation of comparability metrics is all done!");
				}catch(Exception ex){
				    ex.printStackTrace();
				}
			    }		 
/*
 * for non-english language pairs only
 *  targetpath: path to the directory that contains index vectors.
 *  spath: path to the file that lists full path to texts in source languages
 *  tpath: path to the file that lists full path to texts in target languages
 *  outputpath: path to the file that store the comparability results
 *  threshold: only output document pairs with comparability score>threshold.  
 */

		 public void nonENCOSINESimilarityWithoutDocPair(String targetpath, String spath, String tpath, String outputpath, double threshold){
				try{
					System.out.println("start computing comparability score via cosine measure:");
					 BufferedReader br=new BufferedReader(new FileReader(targetpath+File.separator+"index.vectors"));                                            
				     BufferedWriter bw=new BufferedWriter(new FileWriter(outputpath));             
					String s="";
					Hashtable ht=new Hashtable();
				    while (true){
					s=br.readLine();
					if (s==null){
					    break;
					}else{
					    String t[]=s.split("\\	"); //use tab   
					    ht.put(t[0], s);
					 }
				    }
				    ArrayList sdoc=new ArrayList();
				    BufferedReader br1=new BufferedReader(new FileReader(spath)); //path to the input file which lists the full path to the documents to be translated
		     		while (true){
		     			s=br1.readLine();
		     			if (s==null){
		     				break;
		     			}else{		
		     				String names[]=s.replaceAll("\\\\","/").split("/");	
		     				String fullname="";
		     				if (names[0].contains(":")){
		     					names[0]=names[0].replace(":", "@@@");
		     				}
		     				for (int k=0;k<names.length-1;k++){
		     					fullname=fullname+names[k]+"###";
		     				} 
		     				fullname=fullname+names[names.length-1]; 
		     				sdoc.add(fullname);
		     			}
		     		}
		     		br1.close();
		     		ArrayList tdoc=new ArrayList();
				    BufferedReader br2=new BufferedReader(new FileReader(tpath)); //path to the input file which lists the full path to the documents to be translated
		     		while (true){
		     			s=br2.readLine();
		     			if (s==null){
		     				break;
		     			}else{		
		     				String names[]=s.replaceAll("\\\\","/").split("/");	
		     				String fullname="";
		     				if (names[0].contains(":")){
		     					names[0]=names[0].replace(":", "@@@");
		     				}
		     				for (int k=0;k<names.length-1;k++){
		     					fullname=fullname+names[k]+"###";
		     				} 
		     				fullname=fullname+names[names.length-1]; 
		     				tdoc.add(fullname);
		     			}
		     		}
		   //  		long startTime=System.currentTimeMillis();
				    ExecutorService threadExecutor = Executors.newFixedThreadPool(10);
				    for (int i=0;i<sdoc.size();i++){
				    	CountDownLatch threadSignal = new CountDownLatch(tdoc.size());//初始化countDown
				    	for (int j=0;j<tdoc.size();j++){
					         String sn=sdoc.get(i).toString();
					         String tn=tdoc.get(j).toString();
					         nonENCosine cos=new nonENCosine(ht,bw,sn,tn,threshold,threadSignal);
					         threadExecutor.submit(cos);
				    	}
				    	threadSignal.await();
				    	System.out.println((i+1)*tdoc.size() +" document pairs is done!");
				    }
				    bw.flush();
				    bw.close(); 
				    threadExecutor.shutdown();
			    //    long  endTime=System.currentTimeMillis();
				//	 System.out.println("耗时:"+(endTime-startTime)+"毫秒");
			    //    System.out.println("processing time of computing comparbiltiy scores: "+(endTime-startTime)+"milliseconds");
			        System.out.println("The computation of comparability metrics is all done!");
				}catch(Exception ex){
				    ex.printStackTrace();
				}
			    }		 

		 
  
			 
			 

		 private double GETcosine(ArrayList ID1, ArrayList Val1, ArrayList ID2, ArrayList Val2){
			  
				double sim=0;
				double sum1=0;
				for (int i=0;i<Val1.size();i++){
				    double v1=Double.parseDouble(Val1.get(i).toString());
				    sum1=sum1+v1*v1;
				}
				double sum2=0;
				for (int i=0;i<Val2.size();i++){
				    double v2=Double.parseDouble(Val2.get(i).toString());
				    sum2=sum2+v2*v2;
				}
				for (int i=0;i<ID1.size();i++){
				    if (ID2.contains(ID1.get(i).toString())){
					int p=ID2.indexOf(ID1.get(i).toString());
					double v1=Double.parseDouble(Val1.get(i).toString());
					double v2=Double.parseDouble(Val2.get(p).toString());
					sim=sim+v1*v2;
				    }
				}
				sim=sim/(Math.sqrt(sum1)*Math.sqrt(sum2));
				// System.out.println(sim);
				sim=Math.floor(sim*10000+0.5)/10000;
				//  System.out.println(sim);
				return sim;				 
			    }


public void averagescore(){
	try{
	//	BufferedReader br=new BufferedReader(new FileReader("/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/RO-DE/dicmetric/temp/result.txt-old"));
	//	BufferedReader br=new BufferedReader(new FileReader("/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/HR-EN/dicmetric/temp/result.txt"));
		BufferedReader br=new BufferedReader(new FileReader("/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/HR-EN/keyword/keyword.result"));
	//	BufferedReader br=new BufferedReader(new FileReader("/home/fzsu/DicMetric/ro-lt/temp/result.txt-ro2lt"));
		String s="";
		double a=0;
		
		double b=0;
		double c=0;
		int a1=0;
		int a2=0;
		int a3=0;
		int count=0;
		while (true){
			s=br.readLine();
			if (s==null){
				break;
			}else{
				count++;
				String t[]=s.split("\t");
				if (t[2].equals("p")){
					a=a+Double.parseDouble(t[5]);
					a1++;
				}
				if (t[2].equals("cs")){
					b=b+Double.parseDouble(t[5]);
					a2++;
				}
				if (t[2].equals("cw")){
					c=c+Double.parseDouble(t[5]);
					a3++;
				}
			}
		}
		System.out.println(a1+" "+a2+" "+a3+"="+(a1+a2+a3)+" "+count);
		System.out.println(a/a1+" "+b/a2+" "+c/a3);
	}catch(Exception ex){
		ex.printStackTrace();
	}
}




/*
 * check if the result is the same for the approaches with and without multithreading
 */

public void compare(){
	try{
	//	File f=new File("/home/fzsu/tilde-LV-EN/ITLocalizationLV/dicmetric/latvian-translation");
	//    File[] list=f.listFiles();
	//    for (int i=0;i<list.length;i++){
	 //   	BufferedReader br1=new BufferedReader(new FileReader(list[i].getAbsolutePath()));
	 //   	BufferedReader br2=new BufferedReader(new FileReader("/home/fzsu/tilde-LV-EN/ITLocalizationLV/thread/latvian-translation/"+list[i].getName()));
	/*	BufferedReader br1=new BufferedReader(new FileReader("/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/RO-DE/dicmetric/tmp/result"));
    	String s="";
    	ArrayList alist=new ArrayList();
    	while (true){
    		s=br1.readLine();
    		if (s==null){
    			break;
    		}else{
    			alist.add(s);
    		}
    	}
		BufferedReader br2=new BufferedReader(new FileReader("/media/FreeAgent Drive/CTS-machine/fzsu/ICC-stem/RO-DE/dicmetric/tmp/output"));   
		String s1="";
	        String s2="";
	    	while (true){
	       // 	s1=br1.readLine();
	        	s2=br2.readLine();
	        	if (s2==null){
	        		break;
	        	}else{
	        		if (!alist.contains(s2)){
	        	//		System.out.println(s1);
	        			System.out.println(s2);
	        		}
	        	}
	        }  */
	  //  }
	    	System.out.println("done!!!");
	}catch(Exception ex){
		ex.printStackTrace();
	}
}


/*
 *  put all steps in one, for non-english and english language pair
 *  source: source language
 *  target: target language
 *  WN: path to wordnet installation directory
 *   sp: path to file lists full path to texts in source language
 *   tp: path to file lists full path to texts in target language
 *   tempDir: specify a path to a temporary directory (must exist) for storing intermediate outputs 
 *  result: path to the file that store the comparability results for document pairs.
 *  threshold: output document pairs with comparability score>=threshold
 */
			 
public void ENTrack(String source, String target, String WN, String SP, String TP, String tempDir, String result, double threshold){
	   String SL=source.toLowerCase();
	   String TL=target.toLowerCase();
	   translation(SL, "english", SP, tempDir);
		stemmer(SL, tempDir+File.separator+SL+"-translation.txt", WN, tempDir);
		stemmer(TL,TP,WN,tempDir);
		newtext2vectors(SL, TL, tempDir);
		SelectedCOSINESimilarityWithoutDocPair(tempDir, tempDir+File.separator+SL+"-stem", tempDir+File.separator+TL+"-stem", result, threshold);
	
}

/*
 * ALL steps in one, for language pairs that both languages are not English
 * the parameter is the same as above, except the "option" parameter
 * option: directly translate the texts in source language into target language ("0")
 *  or translate both texts in source and target language into English ("1")
 */
public void NonENTrack(String source, String target, String WN, String option, String SP, String TP, String tempDir, String result, double threshold){
	String SL=source.toLowerCase();
	String TL=target.toLowerCase();
	if (option.equals("0")){ // translate from source to target language
			translation(SL, TL, SP, tempDir);
			nonENtext2vectors(SL, TL, TP, tempDir);
			nonENCOSINESimilarityWithoutDocPair(tempDir, SP, TP, result, threshold);
		  }else{
			  translation(SL, "english", SP, tempDir);
			  translation(TL, "english", TP, tempDir);
			  stemmer(SL, tempDir+File.separator+SL+"-translation.txt", WN, tempDir);
			  stemmer(TL, tempDir+File.separator+TL+"-translation.txt", WN, tempDir);
			  newtext2vectors(SL, TL, tempDir);
			  SelectedCOSINESimilarityWithoutDocPair(tempDir, tempDir+File.separator+SL+"-stem", tempDir+File.separator+TL+"-stem", result, threshold);
		  }
}


}
