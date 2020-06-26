package com.example.search_engine.ranker;

import com.example.search_engine.Data.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class Ranker {
    public Hashtable<String, String> countryCodes = new Hashtable<String, String>();
    public String[] searchArray;
    public Data data;
    public int numofdocs;
    public String UserLocation;
    public Boolean wantLocationScore, wantDateScore;

    public Ranker(Data data, String searchString, String Location, Boolean wantLocationScore, Boolean wantDateScore) {
        numofdocs = data.documentsName.size(); // documentsName has same size and indexing as URLs list
        searchArray = searchString.split(" ");
        this.data = data;
        UserLocation = Location;
        countrycodesinit();
        this.wantDateScore = wantDateScore;
        this.wantLocationScore = wantLocationScore;
    }

    public double tf(int wordindex, List<Integer> occurencesOfWordsCount, int docindex, List<String> documentsBody)
    // calculated for each document looking for a single string (one word)
    {
        double documentsBodyCount = documentsBody.get(docindex).split(" ").length;
        double result = 0;
        //for (int i = docindex; i < occurencesOfWordsCount.size(); i+= numofdocs)
        //{
        result = occurencesOfWordsCount.get(docindex + wordindex) / documentsBodyCount;
        //}
        return result;
    }

    public double tf_phrase(List<Integer> countPhraseSearch,List<String> documentsBody,int docindex)
    {
        if(countPhraseSearch.isEmpty() || countPhraseSearch == null)
            return 0.0;
        double documentsBodyCount = documentsBody.get(docindex).split(" ").length;
        double result = 0;
        result = countPhraseSearch.get(docindex);
        return result;
    }

    public double idf(int wordindex, List<Integer> occurencesOfWordsCount)
    // calculated for each word in all the documents
    {
        double n = 0;
        for (int i = wordindex; i < numofdocs + wordindex; i++) {
            n += occurencesOfWordsCount.get(i);
        }

        return Math.log(numofdocs / n);
        // return n;
    }
    public double idf_phrase(List<Integer> countPhraseSearch)
    {
        if(countPhraseSearch.isEmpty() || countPhraseSearch == null)
            return 0.0;
        double n = 0;
        for (int i = 0;i < countPhraseSearch.size();i++)
        {
            n+= countPhraseSearch.get(i);
        }
        return Math.log(numofdocs/n);
    }
    public double tf_idf(int docindex, List<Integer> occurencesOfWordsCount, List<String> documentsBody)
    // calculated for each document for the entirety of all the search string
    {
        double output = 0;
        for (int i = 0; i < searchArray.length; i++) {
            output += tf(i, occurencesOfWordsCount, docindex, documentsBody) * idf(i, occurencesOfWordsCount);

        }
        return output;
    }

    public double tf_idf_phrase(List<Integer> countPhraseSearch,List<String> documentsBody,int docindex)
    {
        return tf_phrase(countPhraseSearch,documentsBody,docindex) * idf_phrase(countPhraseSearch);
    }

    public static void sortValue(Hashtable<String, Double> t) {

        //Transfer as List and sort it
        ArrayList<Map.Entry<String, Double>> l = new ArrayList<Map.Entry<String, Double>>(t.entrySet());
        Collections.sort(l, new Comparator<Map.Entry<String, Double>>() {

            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
    }

    public Set<String> rank(Data data, List<String> Urls) throws IOException {
        Hashtable<String, Double> rankedList = new Hashtable<String, Double>();
        for (int i = 0; i < numofdocs; i++) {
            Double relevenceScore = tf_idf(i, data.occurencesOfWordsCount, data.documentsBody);
            if (wantLocationScore && wantDateScore) {
                relevenceScore += FindCountryCode(Urls.get(i)) + FindsiteDate(Urls.get(i));
                relevenceScore /= Double.valueOf(3);
            } else if (wantLocationScore || wantDateScore) {
                if (wantLocationScore)
                    relevenceScore += FindCountryCode(Urls.get(i));
                else
                    relevenceScore += FindsiteDate(Urls.get(i));
                relevenceScore /= Double.valueOf(2);
            }
            relevenceScore += 2 * tf_idf_phrase(data.countPhraseSearch,data.documentsBody,i); // higher scale for priority
            rankedList.put(Urls.get(i), relevenceScore);
        }
        sortValue(rankedList);
        Set<String> output = rankedList.keySet();
        return output;
    }

    public List<Integer> rankIndices(Data data, List<String> Urls) throws IOException {
        Hashtable<String, Double> rankedList = new Hashtable<String, Double>();
        for (int i = 0; i < numofdocs; i++) {
            Double relevenceScore = tf_idf(i, data.occurencesOfWordsCount, data.documentsBody);
            if (wantLocationScore && wantDateScore) {
                relevenceScore += FindCountryCode(Urls.get(i)) + FindsiteDate(Urls.get(i));
                relevenceScore /= Double.valueOf(3);
            } else if (wantLocationScore || wantDateScore) {
                if (wantLocationScore)
                    relevenceScore += FindCountryCode(Urls.get(i));
                else
                    relevenceScore += FindsiteDate(Urls.get(i));
                relevenceScore /= Double.valueOf(2);
            }
            relevenceScore += 2 * tf_idf_phrase(data.countPhraseSearch,data.documentsBody,i); // higher scale for priority
            rankedList.put(Urls.get(i), relevenceScore);
        }
        sortValue(rankedList);
        Set<String> KeySet = rankedList.keySet();
        List<String> KeyList = new ArrayList<String>(KeySet);
        List<Integer> output = new ArrayList<Integer>();
        for (String Key : KeyList) {
            Integer DocIndex = data.urlsFromIndexer.indexOf(Key);
            output.add(DocIndex);
        }
        return output;
    }

    public Double FindCountryCode(String Url) throws IOException {
        Set<String> keys = this.countryCodes.keySet();
        for (String key : keys) {
            if (Url.contains(key)) {
                return (DownloadPage.distanceRelevence(UserLocation, countryCodes.get(key)));
            }
        }
        return Double.valueOf(0.5); //default value if no country code exists
    }

    public Double FindsiteDate(String URL) {
        double defaultValue = .5;
        try {
            Double output = DownloadPage.dateRelevence(URL);
            return output;
        } catch (Exception e) {
            System.out.println("failed to get date ... returning default value");
            return defaultValue;
        }
    }

    public void countrycodesinit() {
        countryCodes.put(".ac", "Ascension Island");
        countryCodes.put(".ad", "Andorra");
        countryCodes.put(".ae", "United Arab Emirates");
        countryCodes.put(".af", "Afghanistan");
        countryCodes.put(".ag", "Antigua and Barbuda");
        countryCodes.put(".ai", "Anguilla");
        countryCodes.put(".al", "Albania");
        countryCodes.put(".am", "Armenia");
        countryCodes.put(".ao", "Angola");
        countryCodes.put(".ar", "Argentina");
        countryCodes.put(".as", "American Samoa");
        countryCodes.put(".at", "Austria");
        countryCodes.put(".au", "Australia");
        countryCodes.put(".aw", "Aruba");
        countryCodes.put(".ax", "Aland Islands");
        countryCodes.put(".az", "Azerbaijan");
        countryCodes.put(".ba", "Bosnia and Herzegovina");
        countryCodes.put(".bb", "Barbados");
        countryCodes.put(".bd", "Bangladesh");
        countryCodes.put(".be", "Belgium");
        countryCodes.put(".bf", "Burkina Faso");
        countryCodes.put(".bg", "Bulgaria");
        countryCodes.put(".bh", "Bahrain");
        countryCodes.put(".bi", "Burundi");
        countryCodes.put(".bj", "Benin");
        countryCodes.put(".bm", "Bermuda");
        countryCodes.put(".bn", "Brunei");
        countryCodes.put(".bo", "Bolivia");
        countryCodes.put(".br", "Brazil");
        countryCodes.put(".bs", "Bahamas");
        countryCodes.put(".bt", "Bhutan");
        countryCodes.put(".bv", "Bouvet Island");
        countryCodes.put(".bw", "Botswana");
        countryCodes.put(".by", "Belarus");
        countryCodes.put(".bz", "Belize");
        countryCodes.put(".ca", "Canada");
        countryCodes.put(".cc", "Cocos (Keeling) Islands");
        countryCodes.put(".cd", "Congo (Kinshasa)");
        countryCodes.put(".cf", "Central African Republic");
        countryCodes.put(".cg", "Republic of the Congo");
        countryCodes.put(".ch", "Switzerland");
        countryCodes.put(".ci", "Cote d'Ivoire");
        countryCodes.put(".ck", "Cook Islands");
        countryCodes.put(".cl", "Chile");
        countryCodes.put(".cm", "Cameroon");
        countryCodes.put(".cn", "China");
        countryCodes.put(".co", "Colombia");
        countryCodes.put(".cr", "Costa Rica");
        countryCodes.put(".cu", "Cuba");
        countryCodes.put(".cv", "Cabo Verde");
        countryCodes.put(".cw", "Curacao");
        countryCodes.put(".cx", "Christmas Island");
        countryCodes.put(".cy", "Cyprus");
        countryCodes.put(".cz", "Czechia");
        countryCodes.put(".de", "Germany");
        countryCodes.put(".dj", "Djibouti");
        countryCodes.put(".dk", "Denmark");
        countryCodes.put(".dm", "Dominica");
        countryCodes.put(".do", "Dominican Republic");
        countryCodes.put(".dz", "Algeria");
        countryCodes.put(".ec", "Ecuador");
        countryCodes.put(".ee", "Estonia");
        countryCodes.put(".eg", "Egypt");
        countryCodes.put(".er", "Eritrea");
        countryCodes.put(".es", "Spain");
        countryCodes.put(".et", "Ethiopia");
        countryCodes.put(".fi", "Finland");
        countryCodes.put(".fj", "Fiji");
        countryCodes.put(".fk", "Falkland Islands");
        countryCodes.put(".fm", "Federated States of Micronesia");
        countryCodes.put(".fo", "Faroe Islands");
        countryCodes.put(".fr", "France");
        countryCodes.put(".ga", "Gabon");
        countryCodes.put(".gb", "United Kingdom");
        countryCodes.put(".gd", "Grenada");
        countryCodes.put(".ge", "Georgia");
        countryCodes.put(".gf", "French Guiana");
        countryCodes.put(".gg", "Guernsey");
        countryCodes.put(".gh", "Ghana");
        countryCodes.put(".gi", "Gibraltar");
        countryCodes.put(".gl", "Greenland");
        countryCodes.put(".gm", "Gambia");
        countryCodes.put(".gn", "Guinea");
        countryCodes.put(".gp", "Guadeloupe");
        countryCodes.put(".gq", "Equatorial Guinea");
        countryCodes.put(".gr", "Greece");
        countryCodes.put(".gs", "South Georgia and the South Sandwich Islands");
        countryCodes.put(".gt", "Guatemala");
        countryCodes.put(".gu", "Guam (USA)");
        countryCodes.put(".gw", "Guinea-Bissau");
        countryCodes.put(".gy", "Guyana");
        countryCodes.put(".hk", "Hong Kong (China)");
        countryCodes.put(".hm", "Heard Island and McDonald Islands");
        countryCodes.put(".hn", "Honduras");
        countryCodes.put(".hr", "Croatia");
        countryCodes.put(".ht", "Haiti");
        countryCodes.put(".hu", "Hungary");
        countryCodes.put(".id", "Indonesia");
        countryCodes.put(".ie", "Ireland");
        countryCodes.put(".im", "Isle of Man");
        countryCodes.put(".in", "India");
        countryCodes.put(".io", "British Indian Ocean Territory");
        countryCodes.put(".iq", "Iraq");
        countryCodes.put(".ir", "Iran");
        countryCodes.put(".is", "Iceland");
        countryCodes.put(".it", "Italy");
        countryCodes.put(".je", "Jersey");
        countryCodes.put(".jm", "Jamaica");
        countryCodes.put(".jo", "Jordan");
        countryCodes.put(".jp", "Japan");
        countryCodes.put(".ke", "Kenya");
        countryCodes.put(".kg", "Kyrgyzstan");
        countryCodes.put(".kh", "Cambodia");
        countryCodes.put(".ki", "Kiribati");
        countryCodes.put(".km", "Comoros");
        countryCodes.put(".kn", "Saint Kitts and Nevis");
        countryCodes.put(".kp", "North Korea");
        countryCodes.put(".kr", "South Korea");
        countryCodes.put(".kw", "Kuwait");
        countryCodes.put(".ky", "Cayman Islands");
        countryCodes.put(".kz", "Kazakhstan");
        countryCodes.put(".la", "Laos");
        countryCodes.put(".lb", "Lebanon");
        countryCodes.put(".lc", "Saint Lucia");
        countryCodes.put(".li", "Liechtenstein");
        countryCodes.put(".lk", "Sri Lanka");
        countryCodes.put(".lr", "Liberia");
        countryCodes.put(".ls", "Lesotho");
        countryCodes.put(".lt", "Lithuania");
        countryCodes.put(".lu", "Luxembourg");
        countryCodes.put(".lv", "Latvia");
        countryCodes.put(".ly", "Libya");
        countryCodes.put(".ma", "Morocco");
        countryCodes.put(".mc", "Monaco");
        countryCodes.put(".md", "Moldova");
        countryCodes.put(".me", "Montenegro");
        countryCodes.put(".mg", "Madagascar");
        countryCodes.put(".mh", "Marshall Islands");
        countryCodes.put(".mk", "North Macedonia");
        countryCodes.put(".ml", "Mali");
        countryCodes.put(".mm", "Myanmar");
        countryCodes.put(".mn", "Mongolia");
        countryCodes.put(".mo", "Macau");
        countryCodes.put(".mp", "Northern Mariana Islands");
        countryCodes.put(".mq", "Martinique");
        countryCodes.put(".mr", "Mauritania");
        countryCodes.put(".ms", "Montserrat");
        countryCodes.put(".mt", "Malta");
        countryCodes.put(".mu", "Mauritius");
        countryCodes.put(".mv", "Maldives");
        countryCodes.put(".mw", "Malawi");
        countryCodes.put(".mx", "Mexico");
        countryCodes.put(".my", "Malaysia");
        countryCodes.put(".mz", "Mozambique");
        countryCodes.put(".na", "Namibia");
        countryCodes.put(".nc", "New Caledonia (France)");
        countryCodes.put(".ne", "Niger");
        countryCodes.put(".nf", "Norfolk Island (Australia)");
        countryCodes.put(".ng", "Nigeria");
        countryCodes.put(".ni", "Nicaragua");
        countryCodes.put(".nl", "Netherlands");
        countryCodes.put(".no", "Norway");
        countryCodes.put(".np", "Nepal");
        countryCodes.put(".nr", "Nauru");
        countryCodes.put(".nu", "Niue (New Zealand)");
        countryCodes.put(".nz", "New Zealand");
        countryCodes.put(".om", "Oman");
        countryCodes.put(".pa", "Panama");
        countryCodes.put(".pe", "Peru");
        countryCodes.put(".pf", "French Polynesia (France)");
        countryCodes.put(".pg", "Papua New Guinea");
        countryCodes.put(".ph", "Philippines");
        countryCodes.put(".pk", "Pakistan");
        countryCodes.put(".pl", "Poland");
        countryCodes.put(".pm", "Saint Pierre and Miquelon");
        countryCodes.put(".pn", "Pitcairn Islands");
        countryCodes.put(".pr", "Puerto Rico");
        countryCodes.put(".ps", "Palestine");
        countryCodes.put(".pt", "Portugal");
        countryCodes.put(".pw", "Palau");
        countryCodes.put(".py", "Paraguay");
        countryCodes.put(".qa", "Qatar");
        countryCodes.put(".re", "Reunion ");
        countryCodes.put(".ro", "Romania");
        countryCodes.put(".rs", "Serbia");
        countryCodes.put(".ru", "Russia");
        countryCodes.put(".rw", "Rwanda");
        countryCodes.put(".sa", "Saudi Arabia");
        countryCodes.put(".sb", "Solomon Islands");
        countryCodes.put(".sc", "Seychelles");
        countryCodes.put(".sd", "Sudan");
        countryCodes.put(".se", "Sweden");
        countryCodes.put(".sg", "Singapore");
        countryCodes.put(".sh", "Saint Helena");
        countryCodes.put(".si", "Slovenia");
        countryCodes.put(".sj", "Svalbard and Jan Mayen");
        countryCodes.put(".sk", "Slovakia");
        countryCodes.put(".sl", "Sierra Leone");
        countryCodes.put(".sm", "San Marino");
        countryCodes.put(".sn", "Senegal");
        countryCodes.put(".so", "Somalia");
        countryCodes.put(".sr", "Suriname");
        countryCodes.put(".st", "Sao Tome and Principe");
        countryCodes.put(".sv", "El Salvador");
        countryCodes.put(".sx", "Sint Maarten");
        countryCodes.put(".sy", "Syria");
        countryCodes.put(".sz", "Eswatini");
        countryCodes.put(".tc", "Turks and Caicos Islands");
        countryCodes.put(".td", "Chad");
        countryCodes.put(".tf", "French Southern Territories");
        countryCodes.put(".tg", "Togo");
        countryCodes.put(".th", "Thailand");
        countryCodes.put(".tj", "Tajikistan");
        countryCodes.put(".tk", "Tokelau");
        countryCodes.put(".tl", "Timor-Leste");
        countryCodes.put(".tm", "Turkmenistan");
        countryCodes.put(".tn", "Tunisia");
        countryCodes.put(".to", "Tonga");
        countryCodes.put(".tr", "Turkey");
        countryCodes.put(".tt", "Trinidad and Tobago");
        countryCodes.put(".tv", "Tuvalu");
        countryCodes.put(".tw", "Taiwan");
        countryCodes.put(".tz", "Tanzania");
        countryCodes.put(".ua", "Ukraine");
        countryCodes.put(".ug", "Uganda");
        countryCodes.put(".uk", "United Kingdom");
        countryCodes.put(".us", "United States of America");
        countryCodes.put(".uy", "Uruguay");
        countryCodes.put(".uz", "Uzbekistan");
        countryCodes.put(".va", "Vatican City");
        countryCodes.put(".vc", "Saint Vincent and the Grenadines");
        countryCodes.put(".ve", "Venezuela");
        countryCodes.put(".vg", "British Virgin Islands");
        countryCodes.put(".vi", "US Virgin Islands");
        countryCodes.put(".vn", "Vietnam");
        countryCodes.put(".vu", "Vanuatu");
        countryCodes.put(".wf", "Wallis and Futuna");
        countryCodes.put(".ws", "Samoa");
        countryCodes.put(".ye", "Yemen");
        countryCodes.put(".yt", "Mayotte");
        countryCodes.put(".za", "South Africa");
        countryCodes.put(".zm", "Zambia");
        countryCodes.put(".zw", "Zimbabwe");
    }
}