package alix.romans;

import alix.fr.Tokenizer;
import alix.util.Occ;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

/**
 * Tag input xml using alix, gives tagged xml as output
 * 
 * @author odysseuspolymetis
 *
 */

public class tag_xml {

	public static void main(String[] args) throws IOException {

		System.out.println("This software tags TEI/XML files and exports tagged texts to a target directory");

		if (args.length == 0) {

			System.out.println("Usage : tag_xml.java ./path/to/your/source_directory ./path/to/your/target_directory");
			System.exit(1);

		}

		File input_dir = new File(args[0]);
//				File input_dir = new File("/home/odysseus/Bureau/chapitres/git_repo/romans");
		File output_dir = new File(args[1]);
//				File output_dir = new File("/home/odysseus/Bureau/chapitres/git_repo/romans_tagging");
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
					
					p.text("");
					Occ occurrence = new Occ();
					String text_to_tok = p_text.replaceAll("[<>]","");
					Tokenizer tok = new Tokenizer(text_to_tok);
					/* Tokenizing and tagging clean text in text_to_tok */

					while (tok.token(occurrence)) {

						Pattern pattern=Pattern.compile("\\Q"+occurrence.graph().toString()+"\\E",Pattern.CASE_INSENSITIVE);
						Matcher matcher = pattern.matcher(text_to_tok);
						
						/* not efficient at all but still the best way I found so far */

						if (matcher.find() || occurrence.graph().toString().contains("…")) {

							p.appendElement("word").attr("form",occurrence.graph().toString())
								.attr("lemma",occurrence.lem().toString())
								.attr("postag",occurrence.tag().toString());

						}

						else {

							System.out.println("Warning, this occurrence has not been tagged : "+occurrence.graph().toString());

						}

					}

					if (p.ownText().endsWith(".")) {

						p.appendElement("word").attr("form",".")
							.attr("lemma",".")
							.attr("postag","PUN");
						/* The tokenizer wouldn't take into account certain full stops, adding them manually */

					}

				}

			}

			File output = new File(output_dir + "/"+file.getName());
			doc.outputSettings().indentAmount(0).prettyPrint(true);
			doc.outputSettings().syntax(Syntax.xml);
			doc.charset(Charset.forName("utf-8"));
			doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
			PrintWriter writer = new PrintWriter(output,"utf-8");
			writer.write(doc.toString()) ;
			writer.flush();
			writer.close();

			System.out.println("File done : "+file.getName());

		}

	}

}