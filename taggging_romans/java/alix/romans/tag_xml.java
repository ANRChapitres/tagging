package alix.romans;

import alix.fr.Tokenizer;
import alix.util.Occ;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;


public class tag_xml {
	
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerException{
		
		File dir = new File("/home/odysseus/Bureau/ANR/code/global_array/test_files/");
		File[] files_list = dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".xml");
		    }
		});
		
		for (File file : files_list){
			
			String text=new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
			Tokenizer tokenizer=new Tokenizer(text);
			Occ occ = new Occ();
			
			int num_words=0;
			while (tokenizer.token(occ)) {
				if (!occ.graph().toString().contains("§")&&!occ.graph().toString().contains("¶")){
					num_words++;
				}
			}
			
			String list_keys[]=new String [num_words];
			String list_lems[]=new String [num_words];
			String list_tags[]=new String [num_words];
			int index=0;
			
			tokenizer=new Tokenizer(text);
			while (tokenizer.token(occ)) {
				if (!occ.graph().toString().contains("§")&&!occ.graph().toString().contains("¶")){
					list_keys[index]=occ.graph().toString();
					list_lems[index]=occ.lem().toString();
					list_tags[index]=occ.tag().toString();
					index++;
				}
			}
			
			Document doc = Jsoup.parse(text);
			Elements ps = doc.select("p");
			

			System.out.println(ps.size());
			
			for (Element p:ps){
				
				if (p.text().length()>0){
					Occ occurrence = new Occ();
					Tokenizer tok=new Tokenizer(p.text());
					while (tok.token(occurrence)) {
//						System.out.println(occurrence.graph());
//						if (occurrence.graph().toString().length()>0){
							StringBuilder p_text = new StringBuilder(p.text());
							StringBuilder replacement = new StringBuilder();
							replacement.append(occurrence.graph().toString());
							replacement.append(" <word form=");
							replacement.append(occurrence.graph().toString());
							replacement.append("\"> ");
//							System.out.println(occurrence.graph().toString());
//							System.out.println(p_text);
//							System.out.println("premier index : "+p_text.toString().indexOf(occurrence.graph().toString()));
//							System.out.println("deuxième index : "+p_text.toString().indexOf(occurrence.graph().toString())+occurrence.graph().toString().length());
							String final_p=p_text.toString().replaceFirst("^(?!=)\\Q"+occurrence.graph().toString()+"\\E", replacement.toString());
//							if (final_p.indexOf(occurrence.graph().toString())>0){
//								System.out.println("string repérée");
//							}
							p.text(final_p);
//						}
					}
					System.out.println("paragraphe résultant : "+p.text());
				}
			}
			
			
			
			
//			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//			Document doc = dBuilder.parse(file);
//			doc.getDocumentElement().normalize();
//			NodeList nodes=doc.getElementsByTagName("p");
//			
//			for (String key:list_keys){
//				Pattern pattern = Pattern.compile("(!?<)"+key);
//				
//				for (int i = 0; i < nodes.getLength(); i++) {
//					Matcher matcher = pattern.matcher(nodes.item(i).getTextContent());
//					if (matcher.find()){
//						String replacement=nodes.item(i).getTextContent().re;
//						
//					}
//				}
//			}
		}
	}
	
}
