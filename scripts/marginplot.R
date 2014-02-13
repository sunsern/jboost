source("parse.R")

get.pdfs <- function(x, show.scores, show.sep) {
  pdfs <- list()

  for (i in 1:length(x)) {
    pdfs[[i]] <- list()
    margins <- sapply(x[[i]]$data, function(d){return(d$margin)})
    if (show.scores || show.sep) {
      scores <- sapply(x[[i]]$data, function(d){return(d$score)})
      #scores * margins
    } else {
      pdfs[[i]]$x <- sort(margins)
      pdfs[[i]]$y <- (1:length(margins))/length(margins)
    }
  }

  return(pdfs)
}

plot.margin <- function(infofile, specfile, datafile="", iteration=-1, 
                        outname=paste(infofile,"margin","eps",sep="."), 
                        show.scores=F, show.sep=F, ...) {

  # Get the data and needed iterations
  x <- parse.info(infofile, specfile)
  x <- get.iters(x, iteration)

  # Get the cumulative pdfs for the score/margin
  pdfs <- get.pdfs(x,show.scores,show.sep)
  
  # Set up some plot parameters
  xlabel <- "Margins" #   y \\Sigma_t \\alpha h_t(x)"
  if (show.scores) {
    xlabel <- "Scores" #   \\Sigma_t \\alpha h_t(x)"
  }
  ylabel <- "Cumulative Distribution"
  title <- paste("Margins for", infofile)

  # Plot the margin pdfs
  postscript(file=outname)
  plot(0, 0, type="n", xlim=c(-1,1), ylim=c(0,1),
       main=title, xlab=xlabel, ylab=ylabel, ...)
  for (i in 1:length(pdfs)) {
    lines(pdfs[[i]]$x, pdfs[[1]]$y)
  }
  dev.off()
}
