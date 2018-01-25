package alix.romans;

import alix.fr.Tokenizer;
import alix.util.Occ;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;


@SuppressWarnings("deprecation")
public class tag_xml {

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerException {
		
		System.out.println("This software tags TEI/XML files and exports tagged texts to a target director");
		
		if (args.length == 0) {
			System.out.println("Usage : tag_xml.java ./path/to/your/source_directory ./path/to/your/target_directory");
		}

//		File input_dir = new File(args[0]);
		File input_dir = new File("/root/git/tagging/taggging_romans/source_texts");
//		File output_dir = new File(args[1]);
		File output_dir = new File("/root/git/tagging/taggging_romans/target_texts");
		File[] files_list = input_dir.listFiles(new FilenameFilter() {
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

			for (Element p:ps){
				
				String p_text=p.text();
				
				if (p.text().length()>0){
					
					Occ occurrence = new Occ();
					Tokenizer tok=new Tokenizer(p.text());
					
					while (tok.token(occurrence)) {
						
						Pattern pattern=Pattern.compile("\\Q"+occurrence.graph().toString()+"\\E",Pattern.CASE_INSENSITIVE);
						Matcher matcher = pattern.matcher(p.text());
						
						if (matcher.find() || occurrence.graph().toString().contains("…")) {
							
							StringBuilder replacement = new StringBuilder();
							replacement.append(occurrence.graph().toString());
							replacement.append(" <word form=\"");
							replacement.append(occurrence.graph().toString());
							replacement.append(" lemma=\"");
							replacement.append(occurrence.lem().toString());
							replacement.append(" postag=\"");
							replacement.append(occurrence.tag().toString());
							replacement.append("\"> ");
							Element child_word=new Element("word");
							child_word.text(replacement.toString());
							p.appendChild(child_word);
							
						}
						
						else {
							
							System.out.println("Attention, ce terme n'est pas taggé : "+occurrence.graph().toString());
							
						}
						
					}
					
					if (p.ownText().endsWith(".")) {
						
						Element punct=new Element("word");
						punct.text(". <word form=\".\" lemma=\".\" postag=\"PUN\">");
						p.appendChild(punct);
						
					}
					
					p_text=p.text().replaceAll("\\Q"+p.ownText()+"\\E", "");
					p.text(p_text);
					
				}
				
			}
			
			File output = new File(output_dir+"/"+file.getName());
			doc.outputSettings().indentAmount(0).prettyPrint(false);
			@SuppressWarnings("resource")
			PrintWriter writer = new PrintWriter(output,"utf-8");
			writer.write(StringEscapeUtils.unescapeXml(doc.html())) ;

		}
		
	}

}
