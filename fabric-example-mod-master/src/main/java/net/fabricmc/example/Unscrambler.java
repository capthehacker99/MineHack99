package net.fabricmc.example;


import java.io.IOException;

import java.util.ArrayList;

import java.util.Arrays;

import java.util.HashMap;



//Assumptions

//A dictionary of English words should be available

//User will use provide only word with letters as input





//Pseudocode



//English dictionary text file will be available 

//on running the program the use will be asked for the path to the dictionary file



//A hash map will be created as follows

 /*for each word in the dictionary

 	sort the word (in alphabetical order)

 	if sorted string is a key in the hash map

 		add the unsorted word to the value (which is an ArrayList of words)

 	else

 		create a new entry in the hash map with key = sorted string and 

 		add the unsorted word to the value array-list

 	endif

 endfor	

 

 An example hasmap entry is shown below:

 //HashMap containing the letters in alphabetical order for the key and the word

	//Example

	// _____________________________________

	//| Key | Value                         |

	//|eilv | veil , live, levi, evil, vile |

	//

 

//User will enter a word to be unscrambled

//The entered word will be converted to all lower case and then sorted 

 * Use the sorted string as key on the hashmap to find the list of words that can be formed

 * Print this list

*/



public class Unscrambler {

	// path for the dictionary file

	private String dictionaryFile;

	private ArrayList<String> words;

	private HashMap<String, ArrayList<String>> mapwords;



	public Unscrambler(ArrayList<String> wordss) {

		mapwords = new HashMap<String, ArrayList<String>>();
		words = wordss;

		

	}



	public ArrayList<String> getWords(String location) throws IOException {

		
		return words;

	}



	public void PreprocessDictionary() {

		System.out.println("...Pre-processing dictionary...building hashmap");

		ArrayList<String> words = null;

		try {

			words = getWords(dictionaryFile);



			for (int i = 0; i < words.size(); i++) {

				String unsorted = words.get(i);

				char[] str = unsorted.toCharArray();

				Arrays.sort(str);

				String sorted = new String(str);



				ArrayList<String> tempList = mapwords.get(sorted);

				if (tempList == null) {

					ArrayList<String> list = new ArrayList<String>();

					list.add(unsorted);

					mapwords.put(sorted, list);

				} else {

					tempList.add(unsorted);

				}



			}



		} catch (IOException e) {

			System.out.println("Error in PreProcess: "+ e.getMessage());

			

		}



	}



	public String unScrambleWord(String scrambledword) {

		

		char[] input = scrambledword.toCharArray();

		Arrays.sort(input);

	

		ArrayList<String> result = mapwords.get(new String(input));

		
		if(result != null)
		if(result.size() > 0){

			for (String str : result) {
				return str;
			}
			
		}
		return scrambledword;

	}





}