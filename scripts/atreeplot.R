source("parse.R")
source("perlre.R")
library(igraph)


# Example of MultiPrediction
#0       [R] prediction = MultiPrediction.
#prediction 0: BinaryPrediction. p(1)= -0.6496414920651304
#prediction 1: BinaryPrediction. p(1)= -0.2938933324510595
#
#1       [R.0] Splitter = EqualitySplit: 2 highest-degree = phd
#1       [R.0:0] prediction = MultiPrediction.
#prediction 0: BinaryPrediction. p(1)= -0.6206059321542053
#prediction 1: BinaryPrediction. p(1)= 0.9952607782884548
#
#1       [R.0:1] prediction = MultiPrediction.
#prediction 0: BinaryPrediction. p(1)= 0.2326437273762824
#prediction 1: BinaryPrediction. p(1)= -0.9175790854074412
#
#2       [R.0:1.0] Splitter = InequalitySplitter. age < 37.0
#2       [R.0:1.0:0] prediction = MultiPrediction.
#prediction 0: BinaryPrediction. p(1)= -1.5519953894118494
#prediction 1: BinaryPrediction. p(1)= -1.244560833852431
#
#2       [R.0:1.0:1] prediction = MultiPrediction.
#prediction 0: BinaryPrediction. p(1)= 1.8377755517791101
#prediction 1: BinaryPrediction. p(1)= -0.8696119797754972

# Example of binary tree
#0       [R] prediction = BinaryPrediction. p(1)= 0.11523437500000003
#1       [R.0] Splitter = InequalitySplitter. char_freq_bang < 0.0785
#1       [R.0:0] prediction = BinaryPrediction. p(1)= 0.3720703125
#1       [R.0:1] prediction = BinaryPrediction. p(1)= -0.3720703125
#29      [R.0:1.0] Splitter = InequalitySplitter. word_freq_internet < 0.555
#29      [R.0:1.0:0] prediction = BinaryPrediction. p(1)= -0.19531250000000003
#29      [R.0:1.0:1] prediction = BinaryPrediction. p(1)= -0.19531250000000003


plot.atree <- function(treefile, specfile, flip.labels=F, depth=-1, plot.width=-1, plot.height=-1) {
  labels = parse.labels(specfile)
  num.labels = length(labels);
  if (num.labels < 2) {
    show("There don't seem to be enough labels in the spec file...")
    show("Labels found:")
    show(labels)
    return
  }
  show(c("Labels are:", labels))

  lines = readLines(treefile)

  g <- graph.empty()
  g$layout <- layout.reingold.tilford
  index.to.vertex <- list()
  i <- 1
  while (i <= length(lines)) {
    index <- ""
    type <- ""
    label <- ""
  
    line = lines[i]
    #show(sprintf("Line is: %s", line))
    x <- perl.re(line, "^([0-9]+)\\s*\\[(.*)\\].*Splitter\\s*=\\s*(.*)$")
    if (x[1]=="TRUE") {
      #show("Setting type to splitter")
      type = "splitter";
      iteration = x[2];
      index = x[3];
      label = x[4];
      #show(sprintf("Label of splitter is: %s", label))
    }

    x <- perl.re(line, "^([0-9]+)\\s*\\[(.*)\\].*prediction\\s*=\\s*(.*)$")
    if (x[1]=="TRUE") {
      #show("Setting type to predictor")
      type = "predictor";
      iteration = x[2]
      index = x[3]
      label = x[4]
      predType = x[4];
      show(c(type, iteration, index, label))
      x <- perl.re(predType, ".*MultiPrediction.*")
      y <- perl.re(predType, ".*BinaryPrediction.*=(.*)")
      if (x[1]=="TRUE") {
        label = "";
        for (j in 1:num.labels) {
          i <- i + 1
          line = lines[i]
          #prediction 0: BinaryPrediction. p(1)= -0.6496414920651304
          x <- perl.re(line,"^.*prediction.*=\\s*(.*)")
          #show(sprintf("Prediction Binary value is %0.4f", as.numeric(x[2])))
          if (x[1]=="TRUE") {
            pred = x[2]
          } else {
            break
          }
          if (flip.labels) {
            pred = paste("-",pred,sep="")
          }
          label = paste(label, labels[j], ": ", pred, "\n", sep="");
        }
      } else if (y[1]=="TRUE") {
        label = y[2]
        show(sprintf("label is %0.5f", label))
        if(flip.labels) {
          label = as.numeric(label)
          label = - label
          label = as.character(label)
        }
      } else {
        error(paste("Do not recognize prediction type:", label))
        return
      }
    }

    #show(sprintf("Type is: %s", type))
    #show(sprintf("Index is: %s", index))
    #show(sprintf("Label is: %s", label))
  
    if (type=="splitter") {
      g <- add.vertices(g, 1)
      V(g)$frame.color[length(V(g))] <- "blue"
      V(g)$color[length(V(g))] <- "white"
      V(g)$label[length(V(g))] <- paste(iteration, label, sep=": ")
      V(g)$label.cex <- 1.4
      V(g)$shape[length(V(g))] <- "rectangle"
      V(g)$label.color <- "black"

      this.vertex <- length(V(g))-1
      index.to.vertex[[index]] = this.vertex

      parent.index <- substr(index,1,nchar(index)-2)
      parent.vertex <- index.to.vertex[[parent.index]]

      g <- add.edges(g, c(parent.vertex,this.vertex))
      E(g)$color[length(E(g))] <- "black"
      E(g)$width[length(E(g))] <- 5
    }

    if (type=="predictor") {
      g <- add.vertices(g, 1)
      V(g)$frame.color[length(V(g))] <- "yellow"
      V(g)$color[length(V(g))] <- "white"
      V(g)$label[length(V(g))] <- label
      V(g)$shape[length(V(g))] <- "rectangle"
      V(g)$label.cex <- 1
      V(g)$label.color <- "black"

      this.vertex <- length(V(g))-1
      index.to.vertex[[index]] = this.vertex

      if (index!="R") {
        parent.index <- substr(index,1,nchar(index)-2)
        parent.vertex <- index.to.vertex[[parent.index]]

        g <- add.edges(g, c(parent.vertex,this.vertex))

        ans <- substr(index,nchar(index), nchar(index))
        if (ans=="0") {
          E(g)$color[length(E(g))] <- "green"
          E(g)$label[length(E(g))] <- "           Yes"
        } else if (ans=="1") {
          E(g)$color[length(E(g))] <- "red"
          E(g)$label[length(E(g))] <- "No           "
        } else { 
          error(paste("The splitter has an invalid return value!\n 1 or 0 expected, recieved", ans))
        }

        E(g)$label.cex <- 1.5
        E(g)$label.color <- "black"
        E(g)$width[length(E(g))] <- 10
      }
    }
  
    #w <- warnings()
    #if (length(w)>0) {
    #  show(w)
    #}
    i <- i + 1
  }

  # Flip so that the root is at the top
  coords <- g$layout(g)
  coords[,2] <- max(coords[,2])-coords[,2]

  if(plot.width<0) {
    plot.width <- 8
  }
  if(plot.height<0) {
    plot.height <- 9
  }

  fname <- sprintf("%s.eps", treefile)
  show(sprintf("Writing file to '%s'", fname))
  postscript(file=sprintf("%s.eps", treefile), fonts=c("serif", "Palatino"), 
             paper="special", width=plot.width, height=plot.height, horizontal=TRUE)
  plot(1, type="n", axes=FALSE, xlab=NA, ylab=NA, xlim=c(-1,1), ylim=c(-1,1))
  sw <- strwidth(paste("", V(g)$label, ""))
  sh <- strheight(paste("", V(g)$label, ""))
  plot(g, add=TRUE, layout=coords, vertex.size=sw*110, vertex.size2=sh*100*3)
  dev.off()
}
