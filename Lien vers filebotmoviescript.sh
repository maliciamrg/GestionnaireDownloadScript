#!/bin/bash
sudo filebot -script fn:amc --output "/mnt/magneto/videoclub/Film/" --log-file amc.log --action move "/mnt/magneto/videoclub/_incomplet/" -non-strict --def movieFormat="/mnt/magneto/videoclub/Film/{n.replaceAll(/[:]/,'')} ({y})/{n.replaceAll(/[:]/,'')} ({y}, {director}) {vf} {af}" --def subtitles=en,fr --def ut_label=movie --def clean=y --def artwork=y

