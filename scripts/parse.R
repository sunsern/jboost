parse.spec <- function(specfile) {
  lines = readLines(specfile)
  labels = list()
  for (i in 1:length(lines)) {
    line = lines[i]
    line = sub("^.*labels\\s*\\((.*)\\).*$", "labels \\1", line, perl=TRUE)
    if (substr(line, 1, 6)=="labels") {
      labels = unlist(strsplit(substr(line,7,9999999),","))
    }
  }
  return(list(labels=labels))
}



parse.labels <- function(specfile) {
  lines = readLines(specfile)
  labels = list()
  for (i in 1:length(lines)) {
    line = lines[i]
    line = sub("^.*labels\\s*\\((.*)\\).*$", "labels \\1", line, perl=TRUE)
    if (substr(line, 1, 6)=="labels") {
      labels = unlist(strsplit(substr(line,7,9999999),","))
    }
  }
  return(labels)
}


parse.infoline <- function(line, num.classes) {
  s <- unlist(strsplit(line, ":"))
  id <- as.numeric(s[1])
  margin <- as.numeric(s[2])
  if (num.classes <= 2) {
    score <- as.numeric(s[3])
    label <- as.numeric(s[4])
  } else {
    score <- as.numeric(unlist(strsplit(s[3],",")))
    label <- as.numeric(unlist(strsplit(s[4],",")))
  }
  return(list(id=id, margin=margin, score=score, label=label))
}

parse.params <- function(param.line) {
  s <- unlist(strsplit(param.line, ":"))

  iterstr <- unlist(strsplit(s[1], "="))
  iter <- as.numeric(iterstr[2])

  elts <- unlist(strsplit(s[2], "="))
  num.examples <- as.numeric(elts[2])

  boost.params <- unlist(strsplit(s[3], "="))
  boost.params <- c("booster", boost.params[2:length(boost.params)])
  boost.params <- paste(boost.params, collapse="=")

  return( list(iter=iter, num.examples=num.examples, boost.params=boost.params) )
}

parse.info <- function(fname,specname) {
  labels <- parse.labels(specname)
  num.classes <- length(labels)

  con <- file(fname, open="r")
  param.line <- scan(con, what="raw", nlines=1, sep="\n", quiet=T)
  params <- parse.params(param.line)
  num.examples <- params$num.examples

  example.lines <- scan(con, what="raw", nlines=num.examples,  sep="\n", quiet=T)

  #show(example.lines)

  parsed.iters = list()


  i <- 0
  while(TRUE) {
    i <- i + 1
    parsed.iters[[i]] <- list()
    parsed.iters[[i]]$params <- parse.params(param.line)
    parsed.iters[[i]]$data <- lapply(example.lines, parse.infoline, num.classes=num.classes)
    
    param.line <- scan(con, what="raw", nlines=1,  sep="\n", quiet=T)
    if (length(param.line)==0) { break; }
    example.lines <- scan(con, what="raw", nlines=num.examples,  sep="\n", quiet=T)
  }
  close(con)
  return(parsed.iters)
}


get.iters <- function(x, iters) {
  ret <- list()
  if (length(iters)==1) {
    if (iters < 1) {
      ret[[1]] <- x[[length(x)]]
    } else {
      ret[[1]] <- x[[iters]]
    }
  } else {
    i <- 1
    for (j in 1:length(x)) {
      if (j %in% iters) {
        ret[[i]] <- x[[j]]
        i <- i + 1
      }
    }
  }
  return(ret)
}





print.usage <- function() {
    show("\t -i (--info) file.info \t File containing runtime information (required) \n")
    show("\t -s (--spec) file.spec \t Spec file (optional, will default to spec file in info file) \n")
    show("\t -t (--tree) file.tree \t File containing the ADTree in text format (required) \n")
    show("\t -d (--dir)  directory \t Directory to use (optional, defaults to '.') \n")
    show("\t -l (--labels)         \t Flip the labels (-1 becomes +1) (optional)\n")
    show("\t --truncate            \t Truncate threshold values to increase readability\n")
    show("\t --threshold num       \t A given depth to stop the tree from becoming too large. (optional) \n")
    show("\t -h (--help)           \t Print this usage information \n")
}

parse.args <- function(argv) {
  tree = ""
  flip = FALSE
  depth = 0
  spec = ""
  for (i in 1:length(argv)) {
    arg = argv[i]
    if (arg=="--tree") {
      tree = argv[i+1]
    } else if (arg=="--flip") {
      flip = TRUE
    } else if (arg=="--depth") {
      depth = as.numeric(argv[i+1])
    } else if (arg=="--spec") {
      spec = argv[i+1]
    }
  }
  return(list(tree=tree, flip=flip, depth=depth, spec=spec))
}
