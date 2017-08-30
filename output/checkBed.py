#!/usr/bin/python
import sys, os

def usage():
    print("%s file: lift over rs number. " % sys.argv[0] )
    print("file should look like:")
    print("11111")
    print("11112")
    print("...")
def myopen(fn):
    import gzip
    try:
        h = gzip.open(fn)
        ln = h.read(2) # read arbitrary bytes so check if @param fn is a gzipped file
    except:
        # cannot read in gzip format
        return open(fn)
    h.close()
    return gzip.open(fn)

RS_Ref = set()
RS_Input = set() # store rs
RS_CHROM = dict() # high_rs -> (lower_rs, current_rs)
exm = set()
exm_chrom=dict()
if __name__ == '__main__':
 
	import re
	rsPattern = re.compile(r'^[0-9]+$')
	
	for ln in myopen(sys.argv[1]):
		rs = ln.strip()
		if not rsPattern.match(rs):
			print 'ERROR: rs number should be like "1000"'
			sys.exit(2)
			
		RS_Ref.add(rs)

	for ln in myopen(sys.argv[2]):
		rs = ln.strip()
		if not rsPattern.match(rs):
			print 'ERROR: rs number should be like "1000"'
			sys.exit(2)
		# rs number not appear in RS_MERGE -> there is no merge on this rs
		RS_Input.add(rs)	
	
	RS_Diff = RS_Ref.difference(RS_Input)
	
	for ln in myopen(sys.argv[3]):
		ln = ln.strip()
		fd = ln.split("\t")
		if len(fd) < 3:
			continue
		rs = fd[3][2:]

		if rs in  RS_Diff:
			print fd[0],"\t",fd[1],"\t",fd[2], "\t",fd[3]
			