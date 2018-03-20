
/*
 *   Copyright 2015 Cheikh BA <cheikh.ba.sn@gmail.com>
 *
 *   This file is part of POTI.
 *
 *   POTI is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   POTI is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License,
 *   along with POTI.  If not, see <http://www.gnu.org/licenses/>.
*/


/*
 * Created on 13.05.2014
 * Enriching MiniCon algorithm with User Preferences
 * 
 * @author Cheikh BA
 */

package preference;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import minicon.MCD;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class PreferencesFileParser extends DefaultHandler {
	
	private static Hashtable<String, String> MCDRanks;//  = new Hashtable<String, String>();
	private static int preferenceID;
	boolean isCorrespondingPreferenceID = false;
	
	// creer un tableau associatif <vue, rank>, le remplir dans les methodes du parser !!
	// parcourir la liste des MCD et leur associer leurs ranks
	
	public static void setMCDPreferences (List<MCD> mcds, String preferencesFile, int prefID) throws IOException, SAXException{
		
		preferenceID = prefID;		
		MCDRanks = new Hashtable<String, String>();
		
		SAXParserFactory sfactory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = sfactory.newSAXParser();
			XMLReader xmlparser = parser.getXMLReader();
			xmlparser.setContentHandler(new PreferencesFileParser());
			xmlparser.parse(preferencesFile);
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		for (int i = 0 ; i < mcds.size(); i++){
			mcds.get(i).setRank(Double.parseDouble(MCDRanks.get(mcds.get(i).getView().getName())));
		}
	}

	public void startDocument() {
		//System.out.println("********** startDocument ****************");		
	}
	
	public void endDocument() {
		//System.out.println("********** endDocument ****************");		
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes){
		/*System.out.println("* startElement: ");	
		System.out.println("*               uri: " + uri);
		System.out.println("*               localName: " + localName);
		System.out.println("*               qName: " + qName);
		System.out.println("*               attributes: ");
		for (int i = 0; i < attributes.getLength(); i++){
			System.out.println("*                      name = " + attributes.getQName(i));
			System.out.println("*                      value = " + attributes.getValue(attributes.getQName(i)));
		}
		*/
		
		if (qName.equalsIgnoreCase("preference")){
			if (attributes.getValue("id").equalsIgnoreCase("" + preferenceID))
				isCorrespondingPreferenceID = true;
		}
		
		if (isCorrespondingPreferenceID && qName.equalsIgnoreCase("view")){
			MCDRanks.put(attributes.getValue("name"), attributes.getValue("rank"));
		}
	}
	
	public void endElement(String uri, String localName, String qName){
		/*System.out.println("* endElement: ");
		System.out.println("*               uri: " + uri);
		System.out.println("*               localName: " + localName);
		System.out.println("*               qName: " + qName);
		*/
		
		if (qName.equalsIgnoreCase("preference"))
			isCorrespondingPreferenceID = false;
	}
	
	public void warning(SAXParseException e){
		//System.out.println("warning: " + e);
	}
	
	public void error(SAXParseException e){
		//System.out.println("error: " + e);
	}
	
	public void fatalError(SAXParseException e){
		//System.out.println("fatalError: " + e);
	}
	
	/*
	public static void main(String[] args) {

		System.out.println("Parsing ...");
		
		MCDRanks = new Hashtable<String, String>();
		
		preferenceID = 2;
		
		SAXParserFactory sfactory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = sfactory.newSAXParser();
			XMLReader xmlparser = parser.getXMLReader();
			xmlparser.setContentHandler(new PreferencesFileParser());
			xmlparser.parse("preferences.xml");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		System.out.println("MCDRanks:" + MCDRanks);
	}
	*/
}
