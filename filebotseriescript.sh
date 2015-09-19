#!/bin/bash
sudo filebot -script fn:amc --output  "/mnt/magneto/videoclub/Series" --log-file amc.log --action test "/mnt/magneto/videoclub/_Incomplet_series" -non-strict --def animeFormat="/mnt/magneto/videoclub/Series/{n}/Saison {s.pad(2)}/{n} S{s.pad(2)}E{es*.pad(2).join('-E')} ep_{absolute*.pad(3).join('_')} {t}" seriesFormat="/mnt/magneto/videoclub/Series/{n}/Saison {s.pad(2)}/{n} S{s.pad(2)}E{es*.pad(2).join('-E')} ep_{absolute*.pad(3).join('_')} {t}"  --def minFileSize=30 --def clean=y --def artwork=y  --conflict override > filebotseriescript.log
grep -w "\[TEST\] Rename" filebotseriescript.log | while read -r line ; do
    #echo "Processing $line"
	java -cp ./lib/*:./bin com.maliciamrg.gestion.download.Main "$line" "/mnt/magneto/videoclub/Series" "/mnt/magneto/videoclub/_Incomplet_series" "/mnt/magneto/videoclub/_Inconnu_series"
done
sudo filebot -script fn:amc --output  "/mnt/magneto/videoclub/Series" --log-file amc.log --action move "/mnt/magneto/videoclub/_Incomplet_series" -non-strict --def animeFormat="/mnt/magneto/videoclub/Series/{n}/Saison {s.pad(2)}/{n} S{s.pad(2)}E{es*.pad(2).join('-E')} ep_{absolute*.pad(3).join('_')} {t}" seriesFormat="/mnt/magneto/videoclub/Series/{n}/Saison {s.pad(2)}/{n} S{s.pad(2)}E{es*.pad(2).join('-E')} ep_{absolute*.pad(3).join('_')} {t}"  --def minFileSize=30 --def clean=y --def artwork=y  --conflict override > filebotseriescript.log
