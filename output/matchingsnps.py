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
exm_rs = dict()
exm_snp = dict()
if __name__ == '__main__':
 
	import re
	rsPattern = re.compile(r'^[0-9]+$')
	rsP2 = re.compile(r'chr1$')
	rsP3 = re.compile(r'^rs*')
	rsP4 = re.compile(r'^exm*')
	for ln in myopen(sys.argv[1]):
		ln = ln.strip()
		fd = ln.split("\t")
		if not rsP3.match(fd[1]):
			print fd[1],"not matching"
			sys.exit(2)
		
		if not rsP4.match(fd[0]):
			print fd[0],"not matching"
			sys.exit(2)
		
		exm_rs[fd[0].strip()] = fd[1].strip()
		
		print fd[0],fd[1]
	