#!/bin/bash
#
#$ -cwd
#$ -j y
#$ -hard
#$ -S /bin/bash
#$ -l mem_free=2G
#$ -N letter_LogLossBoost_0_0.0_0.0_0.0_5
#
/usr/java/jdk1.5.0_07/bin/java -Xmx1G -cp /home/eettinger/jboost/jboost-2.0r3/dist/jboost.jar:/home/eettinger/jboost/jboost-2.0r3/lib/concurrent.jar jboost.controller.Controller -b LogLossBoost -p 10 -S trial5 -n trial.spec -ATreeType ADD_ROOT_OR_SINGLES -numRounds 2000 -a -1
