package com.maliciamrg.gestion.download;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jcraft.jsch.JSchException;

public class Main {
	public static void main(String[] args) {
		//System.out.println("Start");
		try {
			Param.ChargerParametrage();
			String lineEp = args[0];
			String pathdelabibliothequesdelaserie = args[1];
			if (lineEp.startsWith("[TEST] Rename")) {
				String[] spl = lineEp.substring(15, lineEp.length() - 1).split("\\] to \\[");
				String destmod;

				destmod = correctiondestination(spl[0], spl[1], pathdelabibliothequesdelaserie);

				if (destmod.equals(spl[1])) {
					Ssh.moveFile(spl[0], destmod);
					System.out.println("deplacement:" + spl[0]);
					System.out.println("vers:" + destmod);
				} else {
					System.out.println("contradiction pour :" + spl[0]);
					System.out.println("filebot:" + spl[1]);
					System.out.println("perso  :" + destmod);
				}
			}
		} catch (NumberFormatException | SQLException | InterruptedException | IOException | ParseException | JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Param.cloture();
		} catch (IOException | InterruptedException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("Stop");
	}

	/**
	 * correction des serie avec algorythme perso les episode detective conan
	 * avec seulement le numero absolu on tendance a etre renomer par filebot
	 * comme si la saisn̂ et le numero depisode et&at colle ex : ep 705 = S7E05
	 *
	 * @param pathdelabibliothequesdelaserie
	 * @param lineEp
	 * @throws SQLException
	 * @throws InterruptedException
	 * @throws JSchException
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	private static String correctiondestination(String src, String dest, String pathdelabibliothequesdelaserie) throws SQLException, InterruptedException,
			IOException, NumberFormatException {
		String code0 = "";
		String code1 = "";
		Map<String, String> retepisode = conversionnom2episodes(src, pathdelabibliothequesdelaserie);
		if (!retepisode.get("serie").equals("") && !retepisode.get("saison").equals("000") && !retepisode.get("episode").equals("000")) {
			code0 = " Ep:" + retepisode.get("serie") + " " + retepisode.get("saison") + "-" + retepisode.get("episode")
					+ (retepisode.containsKey("episodebis") ? ("-" + retepisode.get("episodebis")) : ("")) + " ";
		}
		Map<String, String> retepisode2 = conversionnom2episodes(dest, pathdelabibliothequesdelaserie);
		if (!retepisode2.get("serie").equals("") && !retepisode2.get("saison").equals("000") && !retepisode2.get("episode").equals("000")) {
			code1 = " Ep:" + retepisode2.get("serie") + " " + retepisode2.get("saison") + "-" + retepisode2.get("episode")
					+ (retepisode2.containsKey("episodebis") ? ("-" + retepisode2.get("episodebis")) : ("")) + " ";
		}
		if (code0.equals("")) {
			return "";
		} else {
			if (code0.equals(code1)) {
				return dest;
			} else {
				String newname;
				if (retepisode.containsKey("sequentielbis")) {
					newname = pathdelabibliothequesdelaserie + "/" + retepisode.get("serie") + "/Saison "
							+ String.format("%02d", Integer.parseInt(retepisode.get("saison"))) + "/" + retepisode.get("serie") + " S"
							+ String.format("%02d", Integer.parseInt(retepisode.get("saison"))) + "E"
							+ String.format("%02d", Integer.parseInt(retepisode.get("episode"))) + "-E"
							+ String.format("%02d", Integer.parseInt(retepisode.get("episodebis"))) + " ep_"
							+ String.format("%03d", Integer.parseInt(retepisode.get("sequentiel"))) + "_"
							+ String.format("%03d", Integer.parseInt(retepisode.get("sequentielbis"))) + " " + retepisode2.get("partiedroite");
				} else {
					newname = pathdelabibliothequesdelaserie + "/" + retepisode.get("serie") + "/Saison "
							+ String.format("%02d", Integer.parseInt(retepisode.get("saison"))) + "/" + retepisode.get("serie") + " S"
							+ String.format("%02d", Integer.parseInt(retepisode.get("saison"))) + "E"
							+ String.format("%02d", Integer.parseInt(retepisode.get("episode"))) + " ep_"
							+ String.format("%03d", Integer.parseInt(retepisode.get("sequentiel"))) + " " + retepisode2.get("partiedroite");
				}
				return newname.trim();
			}
		}
	}

	/**
	 * Conversionnom2episodes.
	 *
	 * @param fileName
	 *            the file name
	 * @return the map
	 * @throws SQLException
	 *             the SQL exception
	 */
	public static Map<String, String> conversionnom2episodes(String fileName, String path) throws SQLException {
		// Param.logger.debug("episode-" + "decomposerNom " + fileName);
		fileName = getFilePartName(fileName);
		Map<String, String> ret = new HashMap<String, String>();

		String partname = "$$";
		String namecmp = (fileName.toLowerCase() + " ").replaceAll("[+_-]", " ").replaceAll("[(][^)]*[)]", "").replaceAll("[\\[][^\\]]*[\\]]", "");

		Pattern p1 = Pattern
				.compile("([Ss]eason[ ]*|[Ss]|[Ss][Nn])([0-9]{1,2})[ x._-]*([Ee]pisode[ ]*|[Ee]|[Ee][Pp])[ ._-]*([0-9]{0,2})[ ]*[Ee&x._-]([0-9]{0,2})[ ._-]");
		Pattern p6 = Pattern.compile("([Ss]eason[ ]*|[Ss]|[Ss][Nn])([0-9]{1,2})[ x._-]*([Ee]pisode[ ]*|[Ee]|[Ee][Pp])[ ._-]*([0-9]{0,2})[ ._-]");
		Pattern p5 = Pattern.compile("([Ss]eason[ ]*|[Ss]|[Ss][Nn])([0-9]{1,2})[ x._-]*([Ee]pisode[ ]*|[Ee]|[Ee][Pp])[ ._-]*([0-9]{3,3})[ ._-]");
		Pattern p2 = Pattern.compile("[._ (-]([0-9]+)x([0-9]+)");
		// Pattern p3 = Pattern.compile("[._ (-]([0-9]+)([0-9][0-9])");
		Pattern p4 = Pattern.compile("[np._ (-]([0-9]+)[._ (-]+([0-9]+)*");

		Matcher m1 = p1.matcher(namecmp.toLowerCase());
		Matcher m2 = p2.matcher(namecmp.toLowerCase());
		// Matcher m3 = p3.matcher(namecmp.toLowerCase());
		Matcher m4 = p4.matcher(namecmp.toLowerCase());
		Matcher m5 = p5.matcher(namecmp.toLowerCase());
		Matcher m6 = p6.matcher(namecmp.toLowerCase());

		HashMap<String, String> numeroEpisodeTrouve = new HashMap<String, String>();
		HashMap<String, String> numeroSequentielTrouve = new HashMap<String, String>();
		HashMap<String, String> numeroSaisonTrouve = new HashMap<String, String>();
		HashMap<String, String> nometextension = new HashMap<String, String>();
		numeroEpisodeTrouve.clear();
		numeroSequentielTrouve.clear();
		numeroSaisonTrouve.clear();
		if (m4.find()) {
			numeroEpisodeTrouve.clear();
			numeroSequentielTrouve.clear();
			numeroSaisonTrouve.clear();
			partname = namecmp.substring(0, m4.start(0));
			numeroSequentielTrouve.put("sequentiel", String.format("%03d", Integer.parseInt(m4.group(1))));
			if (m4.groupCount() > 1) {
				String seq2 = m4.group(2);
				if (seq2 != null) {
					if (!seq2.equals("")) {
						numeroSequentielTrouve.put("sequentielbis", String.format("%03d", Integer.parseInt(m4.group(2))));
					}
				}
			}
			if (!nometextension.containsKey("partiedroite")) {
				nometextension.put("partiedroite", namecmp.substring(m4.end()));
			}
			// Param.logger.debug("episode-" + "decomposerNom 4-" + partname +
			// " " + numeroSaisonTrouve.toString() + " " +
			// numeroEpisodeTrouve.toString() + " "
			// + numeroSequentielTrouve.toString());
		}

		// if (m3.find())
		// {
		// partname = namecmp.substring(0, m3.start(0));
		// numeroSaisonTrouve.put("saison", String.format("%03d",
		// Integer.parseInt(m3.group(1).toString())));
		// numeroEpisodeTrouve.put("episode", String.format("%03d",
		// Integer.parseInt(m3.group(2).toString())));
		// //Param.logger.debug("episode-" + "decomposerNom 3-" + partname + " "
		// + numeroSaisonTrouve.toString() + " " +
		// numeroEpisodeTrouve.toString() + " "
		// // + numeroSequentielTrouve.toString());
		// }

		if (m2.find()) {
			numeroEpisodeTrouve.clear();
			numeroSaisonTrouve.clear();
			numeroSequentielTrouve.clear();
			partname = namecmp.substring(0, m2.start(0));
			numeroSaisonTrouve.put("saison", String.format("%03d", Integer.parseInt(m2.group(1).toString())));
			numeroEpisodeTrouve.put("episode", String.format("%03d", Integer.parseInt(m2.group(2).toString())));
			if (!nometextension.containsKey("partiedroite")) {
				nometextension.put("partiedroite", namecmp.substring(m2.end()));
			}
			// Param.logger.debug("episode-" + "decomposerNom 2-" + partname +
			// " " + numeroSaisonTrouve.toString() + " " +
			// numeroEpisodeTrouve.toString() + " "
			// + numeroSequentielTrouve.toString());
		}

		if (m5.find()) {
			numeroEpisodeTrouve.clear();
			numeroSaisonTrouve.clear();
			numeroSequentielTrouve.clear();
			partname = namecmp.substring(0, m5.start(0));
			numeroSaisonTrouve.put("saison", String.format("%03d", Integer.parseInt(m5.group(2).toString())));
			numeroEpisodeTrouve.put("episode", String.format("%03d", Integer.parseInt(m5.group(4).toString())));
			if (!nometextension.containsKey("partiedroite")) {
				nometextension.put("partiedroite", namecmp.substring(m5.end()));
			}
			// Param.logger.debug("episode-" + "decomposerNom 5-" + partname +
			// " " + numeroSaisonTrouve.toString() + " " +
			// numeroEpisodeTrouve.toString() + " "
			// + numeroSequentielTrouve.toString());
		} else {
			if (m6.find()) {
				numeroEpisodeTrouve.clear();
				numeroSaisonTrouve.clear();
				// numeroSequentielTrouve.clear();
				partname = namecmp.substring(0, m6.start(0));
				numeroSaisonTrouve.put("saison", String.format("%03d", Integer.parseInt(m6.group(2).toString())));
				numeroEpisodeTrouve.put("episode", String.format("%03d", Integer.parseInt(m6.group(4).toString())));
				if (!nometextension.containsKey("partiedroite")) {
					nometextension.put("partiedroite", namecmp.substring(m6.end()));
				}
				// Param.logger.debug("episode-" + "decomposerNom 6-" + partname
				// + " " + numeroSaisonTrouve.toString() + " " +
				// numeroEpisodeTrouve.toString() + " "
				// + numeroSequentielTrouve.toString());
			}
			if (m1.find()) {
				if (m1.group(5).toString().compareTo("") != 0) {
					if (isNumeric(m1.group(4).toString()) && isNumeric(m1.group(5).toString())) {
						if (Integer.parseInt(m1.group(5).toString()) == (Integer.parseInt(m1.group(4).toString()) + 1)) {
							numeroEpisodeTrouve.clear();
							numeroSaisonTrouve.clear();
							numeroSequentielTrouve.clear();
							partname = namecmp.substring(0, m1.start(0));
							numeroSaisonTrouve.put("saison", String.format("%03d", Integer.parseInt(m1.group(2).toString())));
							numeroEpisodeTrouve.put("episode", String.format("%03d", Integer.parseInt(m1.group(4).toString())));
							numeroEpisodeTrouve.put("episodebis", String.format("%03d", Integer.parseInt(m1.group(5).toString())));
							if (!nometextension.containsKey("partiedroite")) {
								nometextension.put("partiedroite", namecmp.substring(m1.end()));
							}
							// Param.logger.debug("episode-" +
							// "decomposerNom 1-" + partname + " " +
							// numeroSaisonTrouve.toString() + " "
							// + numeroEpisodeTrouve.toString() + " " +
							// numeroSequentielTrouve.toString());
						}
					}
				}
			}
		}

		Integer nbtrouve = 0;
		// numeroSaisonTrouve.remove("saison", "000");
		// numeroEpisodeTrouve.remove("episode", "000");
		// numeroSequentielTrouve.remove("sequentiel", "000");
		if ((numeroSaisonTrouve.size() > 0 && numeroEpisodeTrouve.size() > 0) || numeroSequentielTrouve.size() > 0) {

			Boolean ctrlnom;
			ArrayList<String> listrepertoire = Ssh.getRemoteRepertoire(path);
			for (String repSerie : listrepertoire) {
				String nomserie = repSerie.replace(path + "/", "");
				// ResultSet rs = null;
				// Statement stmt = Param.con.createStatement();
				// rs = stmt.executeQuery("SELECT * " + " FROM series " + "  ");
				// while (rs.next()) {
				ctrlnom = true;
				// String textSerieNettoyer =
				// rs.getString("nom").replaceAll("[(]([0-9a-zA-Z]*)[)]", "");
				String textSerieNettoyer = nomserie.replaceAll("[(]([0-9a-zA-Z]*)[)]", "");
				String[] textSerie = textSerieNettoyer.split("[-,'._() ]+");
				String partnameDouble = partname.replaceAll("(.)(?=\\1)", "");
				for (String mot : textSerie) {
					if (mot.length() > 1) {
						String motDouble = mot.replaceAll("(.)(?=\\1)", "");
						if ((" " + partname).indexOf(mot.toLowerCase()) < 1 && (" " + partnameDouble).indexOf(motDouble.toLowerCase()) < 1) {
							ctrlnom = false;
						}
					}
				}
				if (ctrlnom) {
					nbtrouve++;
					ret.put("serie", nomserie);
					/*
					 * String where; if ((numeroSaisonTrouve.size() == 0 &&
					 * numeroEpisodeTrouve.size() == 0)) { where =
					 * " sequentiel = " +
					 * numeroSequentielTrouve.get("sequentiel"); if
					 * (numeroSequentielTrouve.get("sequentielbis") != null) {
					 * where += " OR sequentiel = " +
					 * numeroSequentielTrouve.get("sequentielbis"); } ResultSet
					 * rs2 = null; Statement stmt2 =
					 * Param.con.createStatement(); rs2 =
					 * stmt2.executeQuery("SELECT * " + " FROM episodes " +
					 * " where " + where); while (rs2.next()) {
					 * numeroSaisonTrouve.put("saison", String.format("%03d",
					 * Integer.parseInt(rs2.getString("num_saison")))); if
					 * (numeroEpisodeTrouve.size() == 0) {
					 * numeroEpisodeTrouve.put("episode", String.format("%03d",
					 * Integer.parseInt(rs2.getString("num_episodes")))); } else
					 * { numeroEpisodeTrouve.put("episodebis",
					 * String.format("%03d",
					 * Integer.parseInt(rs2.getString("num_episodes")))); } } }
					 */
					// Param.logger.debug("episode- decomposerNom" +
					// rs.getString("nom"));
				}

				// }
				// rs.close();

			}
			ret.putAll(numeroSaisonTrouve);
			ret.putAll(numeroEpisodeTrouve);
			ret.putAll(numeroSequentielTrouve);
			ret.putAll(nometextension);
		}

		if (nbtrouve == 0) {
			ret.put("serie", "");
		}
		if (!ret.containsKey("saison")) {
			ret.put("saison", "000");
		}
		if (!ret.containsKey("episode")) {
			ret.put("episode", "000");
		}
		if (!ret.containsKey("sequentiel")) {
			ret.put("sequentiel", "000");
		}
		return ret;

	}

	/**
	 * Gets the file part name.
	 *
	 * @param fileName
	 *            the file name
	 * @return the file part name
	 */
	public static String getFilePartName(String fileName) {
		int pos;
		pos = fileName.lastIndexOf("/");
		if (pos > 0) {
			fileName = fileName.substring(pos + 1);
		}
		pos = fileName.lastIndexOf("\\");
		if (pos > 0) {
			fileName = fileName.substring(pos + 1);
		}
		return fileName;
	}

	/**
	 * Checks if is numeric.
	 *
	 * @param s
	 *            the s
	 * @return true, if is numeric
	 */
	public static boolean isNumeric(String s) {
		return s.matches("[-+]?\\d*\\.?\\d+");
	}
}
