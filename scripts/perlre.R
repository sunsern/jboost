perl.re <- function(str, expr) {
  ret = c("FALSE")
  #show(c(str, expr))
  is.present <- grep(expr, c(str,"as"), perl=TRUE)
  #show(is.present)
  if (length(is.present) > 0) {
    # get number of captures
    expr <- unlist(strsplit(expr, NULL))
    #show(expr)
    out <- "TRUE"
    k <- 0
    for (i in 1:length(expr)) {
      if (paste(expr[i-1],expr[i],sep="")=="\\(") { next; }
      if (expr[i]=="(") {
        k <- k + 1
        out = paste(out, paste("\\",k,sep=""), sep="|")
        #show(out)
      }
    }
    expr <- paste(expr, collapse="")
    ret <- unlist(strsplit(sub(expr, out, str), "|"))
    #show(c(expr, out, str))
    ret <- sub(expr, out, str)
    ret <- unlist(strsplit(ret, "\\|"))
    #show(ret)
  }
  return(ret)
}

