default: build

latex=pdflatex -interaction=nonstopmode -file-line-error -synctex=1 -output-format=pdf

build:
	$(latex) notes.tex
	makeglossary notes
	$(latex) notes.tex

clean:
	rm -rf notes.pdf notes.glo notes.log notes.log notes.aux notes.glg notes.gls notes.ist notes.out notes.toc \
	notes.synctex.gz
