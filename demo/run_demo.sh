#!/bin/bash
java -Xmx500M jboost.controller.Controller -S spambase -b AdaBoost -numRounds 100
