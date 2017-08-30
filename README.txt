README
----------------------------------------------------------------------------------------
!!! The application needs minimum ~15GB of free disk space to download and process the bed files from the NCBI ftp server!!!

-----------------USAGE------------------------------------------------------------------
1. runme_first.sh downloads hg19 snp bed files from ncbi ftp server, run md5checksum on all files then sort them before being used.
  i. If running in Windows:
	a. go to https://cygwin.com/install.html
	b. Install Cygwin by running setup-x86_(32 bit or 64bit).exe 
	c. In the Select Packages phase during setup, search for 'wget' in 'Not Installed' view. 
	d. Install the Web category package (check Bin?)
	e. Click 'yes' to all further prompts and finish the installation
	f. Run cygwin. CD into the application directory and run ./runme_first.sh
	
  ii. If running in Linux:
	a. Go to CLI and cd into the application directory 
	b. Run ./runme_first.sh


2.	If input bed file uses Hg38 assembly and you need to convert genome coordinates and annotations to Hg19:
	
		Web version of LiftOver is available at this address: http://genome.ucsc.edu/cgi-bin/hgLiftOver
	
4. The script will download all 24 chromosome bed files containing snps, check MD5 sums, and sort them by chromosome end positions
	
5. Once script is finished, type ./wrapper.sh -i1[first_input_bedfile] -i2[second_input_bedfile]
	
----------INPUT FILE FORMAT------------------------------------------------------------------
Each row has three columns. 
First column must start with the prefix 'chr' followed by number from 1-22 or Y or Y or M or MT
Second column must be an integer value of the chromosome start position
Second column must be an integer value of the chromosome end position
Each column is tab delimited

|--Chromosome Number --|--Chromosome Start Position--|--Chromosome End Position--|
e.g  Chr1			tab delimited		12345				tab delimited			54321
e.g  ChrM					123								345
----------OUTPUT FILES------------------------------------------------------------------

snps1.rs - contains all snps that are in chromosome range from input1 bed file

snps2.rs - contains all snps that are in chromosome range from input2 bed file

matchedSnps_remapped.rs - contains matchingSnps based on the latest snp merge history

---------CORNER CASE-------------------------------------------------------------------
Few rsIDs (maybe they are not SNPs) have chromosome ranges longer than our scan range

|---------------------| (rs range)
  |--------------|      (chromosome range we want to scan)
  
These corner cases are ignored

----------- Overview of the application------------------------------------------------
1. The java app sorts all chromosome ranges that we want to scan
2. Linear search is possible because both the chromosome ranges that we want to scan and the bed files are sorted by chromosome end positions
3. The tool is multithreaded and will simulateneously launch 4 threads in parallel
4. The snps found in the scan ranges from both input files are 'lifted' based on the latest merge history file downloaded from NCBI ftp server
5. The overlapping set of snps between both list of snps is returned as the final output