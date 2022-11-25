package lib.pursuer.stringutils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class WildcardMatcher {
	private String ptn;
	private Pattern regptn;
	private Character[] filterWords = new Character[] { '\\', '/', '.', '+', '$', '^', '[', ']', '(', ')', '{', '}',
			'|' };

	public WildcardMatcher(String pattern) {
		ptn = pattern;
		parse();
	}

	private boolean parse() {

		String regStr = protectKeyword(ptn);
		regStr = regStr.replace("*", ".*");
		regStr = regStr.replace("?", ".{1}");
		try {
			regptn = Pattern.compile(regStr);
			return true;
		} catch (PatternSyntaxException e) {
			return false;
		}
	}

	public String getPattern() {
		return ptn;
	}

	private String protectKeyword(String p) {
		StringBuffer regSb = new StringBuffer(ptn);
		List<Character> ls = Arrays.asList(filterWords);
		for (int i = 0; i < regSb.length(); i++) {
			Character tch = regSb.charAt(i);
			if (ls.contains(tch)) {
				regSb.insert(i, '\\');
				i++;
			}
		}
		return regSb.toString();
	}

	public boolean match(String input) {
		Matcher match = regptn.matcher(input);
		return match.matches();
	}
}
