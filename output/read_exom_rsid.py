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

if __name__ == '__main__':
 
	import re
	rsPattern = re.compile(r'^[0-9]+$')
	rsP2 = re.compile(r'chr1$')
	rsP3 = re.compile(r'^rs*|.$')
	rsP4 = re.compile(r'^exm*')
	#input InfiniumExome-24v1-0_A1_b138_rsids.txt
	for ln in myopen(sys.argv[1]):
		ln = ln.strip()
		fd = ln.split("\t")
		if not rsP3.match(fd[1]):
			print fd[1],"not matching"
			sys.exit(2)
		
		if not rsP4.match(fd[0]):
			print fd[0],"not matching"
			sys.exit(2)
		
		if fd[1].find(','):
			rses= fd[1].split(',')
			exm_rs[fd[0].strip()] = rses
		else:
			exm_rs[fd[0].strip()] = fd[1].strip()
		
		#print fd[0],fd[1]
	#input InfiniumExome-24v1-0_A1.bed
	for ln in myopen(sys.argv[2]):
		ln = ln.strip()
		
		fd = ln.split("\t")
		exm = fd[3].strip()
		#print fd[3]
		if exm in exm_rs:
			rs = exm_rs[exm]
			if isinstance(rs,list):
				for x in rs:
					RS_CHROM[x]= fd[0],fd[1],fd[2]
			else:
				RS_CHROM[rs]= fd[0],fd[1],fd[2]
	
	#input diff.txt
	for ln in myopen(sys.argv[3]):
		rs = ln.strip()
		if rs not in RS_CHROM:
			print rs+" not found in RS_CHROM"
			sys.exit(2)
		
		chrNum, chrStart, chrEnd = RS_CHROM[rs]
		chrNum = chrNum[3:]
		print chrNum
		fileName="sorted_bed_chr_"+chrNum+".bed"
	
		for ln in myopen(fileName):
			rsP5 = re.compile(r'chr*$')
			if not rsP5.match(ln):
				continue
				
			print ln	
			ln = ln.strip()
			fd = ln.split("\t")
			rsRef = fd[3].strip()
			r_chrStart = fd[1].strip()
			r_chrEnd = fd[2].strip()
			
			if rsRef == rs:
				if not r_chrStart == chrStart:
					print rs+" does not have matching chromosome start positions:"
					print "r_chrStart=",r_chrStart,"chrStart=",chrStart
			
				if not r_chrEnd == chrEnd:
					print rs+" does not have matching chromosome end positions:"
					print "r_chrEnd=",r_chrEnd,"chrEnd=",chrEnd
				
				print rs,"start and end positions match"
	