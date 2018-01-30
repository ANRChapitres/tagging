package alix.romans;

import alix.fr.Tokenizer;
import alix.util.Occ;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;


@SuppressWarnings("deprecation")
public class tag_xml {

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerException {

		System.out.println("This software tags TEI/XML files and exports tagged texts to a target directory");

		if (args.length == 0) {

			System.out.println("Usage : tag_xml.java ./path/to/your/source_directory ./path/to/your/target_directory");
			System.exit(1);

		}

		new Tag(args);

	}

}

class Tag {

	@SuppressWarnings({ "deprecation" })
	public Tag(String args[]) throws FileNotFoundException, UnsupportedEncodingException{

		File input_dir = new File(args[0]);
		//		File input_dir = new File("/home/odysseus/Bureau/ANR/code/corpus/romans");
		File output_dir = new File(args[1]);
		//		File output_dir = new File("/home/odysseus/git/tag_xml/taggging_romans/target_texts");
		File[] files_list = input_dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xml");
				/* Lists all the files in source directory with xml extension */
			}

		});

		for (File file : files_list){

			String text = "";

			try {

				text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).replaceAll("’", "'");
				text = text.replaceAll("&nbsp;", " ");
				text = text.replaceAll("&", "et");
				/* gets rid of potential wrongdoers for the xml format */

			} catch (IOException e) {

				e.printStackTrace();

			}

			Document doc = Jsoup.parse(text,"", Parser.xmlParser());
			Elements ps = doc.select("p");

			for (Element p:ps){

				String p_text = p.text();

				if (p.text().length()>0){

					Occ occurrence = new Occ();
					String text_to_tok = p.text().replaceAll("[<>]","");
					Tokenizer tok = new Tokenizer(text_to_tok);
					/* Tokenizing and tagging clean text in text_to_tok */

					while (tok.token(occurrence)) {

						Pattern pattern=Pattern.compile("\\Q"+occurrence.graph().toString()+"\\E",Pattern.CASE_INSENSITIVE);
						Matcher matcher = pattern.matcher(text_to_tok);
						
						/* not efficient at all but still the best way I found so far */

						if (matcher.find() || occurrence.graph().toString().contains("…")) {

							StringBuilder replacement = new StringBuilder();
							replacement.append(" <word form=\"");
							replacement.append(occurrence.graph().toString());
							replacement.append("\" lemma=\"");
							replacement.append(occurrence.lem().toString());
							replacement.append("\" postag=\"");
							replacement.append(occurrence.tag().toString());
							replacement.append("\">");
							replacement.append(occurrence.graph().toString());
							replacement.append("</word>");
							Element child_word=new Element("word");
							child_word.text(replacement.toString());
							p.appendChild(child_word);

						}

						else {

							System.out.println("Warning, this occurrence has not been tagged : "+occurrence.graph().toString());

						}

					}

					if (p.ownText().endsWith(".")) {

						Element punct = new Element("word");
						punct.text("<word form=\".\" lemma=\".\" postag=\"PUN\">.</word>");
						p.appendChild(punct);
						/* The tokenizer wouldn't take into account certain full stops, adding them manually */

					}

					p_text = p.text().replace(p.ownText(), "");
					/* this does not always work due to encoding problems */
					p.text(p_text);

				}

			}

			File output = new File(output_dir + "/"+file.getName());
			doc.outputSettings().indentAmount(0).prettyPrint(false);
			PrintWriter writer = new PrintWriter(output,"utf-8");
			writer.write(StringEscapeUtils.unescapeXml(doc.toString())) ;
			writer.flush();
			writer.close();

			System.out.println("File done : "+file.getName());

		}

	}

}