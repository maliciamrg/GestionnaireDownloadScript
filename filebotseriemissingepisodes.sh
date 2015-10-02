#!/bin/bash
for f in /mnt/magneto/videoclub/Series/*;
  do
     if [ -d "$f" ] ; then
		echo "$f"
		nserie=$(basename "$f")
		filebot -script /mnt/diablo/kitchen/source_code/MissingEpisodes/MissingEpisodes.groovy "$f/" "$f/$nserie.txt" > "$f/$nserie.csv" 
	 fi
  done;
