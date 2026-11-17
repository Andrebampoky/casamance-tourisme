#!/usr/bin/env python3
from pathlib import Path
import re, sys
sys.path.insert(0, '/tmp/casamance-python-docx')
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_CELL_VERTICAL_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

HERE=Path(__file__).resolve().parent
SRC=HERE/'rapport.md'
OUT=HERE/'rapport.docx'
UML=HERE.parent/'uml'
doc=Document()
sec=doc.sections[0]
sec.top_margin=Inches(.72); sec.bottom_margin=Inches(.72); sec.left_margin=Inches(.82); sec.right_margin=Inches(.82)
styles=doc.styles
styles['Normal'].font.name='Calibri'; styles['Normal'].font.size=Pt(10); styles['Normal'].font.color.rgb=RGBColor(35,45,38)
for key,size in [('Title',30),('Heading 1',22),('Heading 2',15),('Heading 3',12)]:
    st=styles[key]; st.font.name='Calibri'; st.font.size=Pt(size); st.font.bold=True; st.font.color.rgb=RGBColor(23,85,52)
styles['Normal'].paragraph_format.space_after=Pt(6)
# Header and page numbering
header=sec.header.paragraphs[0]; header.text='CASAMANCE TOURISME  |  RAPPORT DE PROJET'; header.style='Caption'
footer=sec.footer.paragraphs[0]; footer.alignment=WD_ALIGN_PARAGRAPH.CENTER
footer.add_run('Licence 3 Informatique  •  ')
fld=OxmlElement('w:fldSimple'); fld.set(qn('w:instr'),'PAGE'); footer._p.append(fld)

def shade(cell,fill):
    pr=cell._tc.get_or_add_tcPr(); shd=OxmlElement('w:shd'); shd.set(qn('w:fill'),fill); pr.append(shd)
def inline(p,s):
    # Preserve bold/code emphasis; links are represented by their visible label.
    pieces=re.split(r'(\*\*.*?\*\*|`.*?`|\[[^\]]+\]\([^)]+\))',s)
    for x in pieces:
        if not x: continue
        if x.startswith('**') and x.endswith('**'): p.add_run(x[2:-2]).bold=True
        elif x.startswith('`') and x.endswith('`'):
            r=p.add_run(x[1:-1]); r.font.name='Consolas'; r.font.size=Pt(9)
        elif x.startswith('['): p.add_run(re.sub(r'\[([^\]]+)\]\([^)]+\)',r'\1',x))
        else: p.add_run(x)
def add_diagram(name,caption):
    path=UML/name
    p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.CENTER
    p.add_run().add_picture(str(path),width=Inches(6.1))
    c=doc.add_paragraph(caption); c.alignment=WD_ALIGN_PARAGRAPH.CENTER
    for r in c.runs: r.italic=True; r.font.size=Pt(8); r.font.color.rgb=RGBColor(80,95,85)

lines=SRC.read_text(encoding='utf-8').splitlines(); i=0; code=False; codebuf=[]; cover=True; titlecount=0
while i<len(lines):
    line=lines[i]
    if line.startswith('```'):
        if code:
            p=doc.add_paragraph(); p.paragraph_format.left_indent=Inches(.2)
            r=p.add_run('\n'.join(codebuf)); r.font.name='Consolas'; r.font.size=Pt(8)
            codebuf=[]; code=False
        else: code=True
        i+=1; continue
    if code: codebuf.append(line); i+=1; continue
    if line.startswith('|') and i+1<len(lines) and re.match(r'^\|[\s|:-]+\|\s*$',lines[i+1]):
        headers=[x.strip() for x in line.strip('|').split('|')]; rows=[]; i+=2
        while i<len(lines) and lines[i].startswith('|'):
            rows.append([x.strip() for x in lines[i].strip('|').split('|')]); i+=1
        t=doc.add_table(rows=1,cols=len(headers)); t.alignment=WD_TABLE_ALIGNMENT.CENTER; t.style='Table Grid'
        for j,h in enumerate(headers):
            cell=t.rows[0].cells[j]; shade(cell,'175534'); cell.vertical_alignment=WD_CELL_VERTICAL_ALIGNMENT.CENTER
            run=cell.paragraphs[0].add_run(h); run.bold=True; run.font.color.rgb=RGBColor(255,255,255); run.font.size=Pt(8)
        for row in rows:
            cells=t.add_row().cells
            for j,val in enumerate(row[:len(headers)]):
                cells[j].text=re.sub(r'`([^`]+)`',r'\1',val)
                for run in cells[j].paragraphs[0].runs: run.font.size=Pt(8)
        continue
    if not line.strip(): i+=1; continue
    if line.startswith('# '):
        p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.CENTER; p.paragraph_format.space_before=Pt(95 if titlecount==0 else 5)
        r=p.add_run(line[2:]); r.bold=True; r.font.size=Pt(28 if titlecount==0 else 16); r.font.color.rgb=RGBColor(22,75,48)
        titlecount+=1
        if titlecount==2: doc.add_page_break()
    elif line.startswith('## '):
        if re.match(r'## (5\.|9\.|14\.|20\.|25\.)',line): doc.add_page_break()
        doc.add_heading(line[3:],level=2)
        if line.startswith('## 5. '): add_diagram('architecture.png','Figure 1 — Architecture microservices déployée')
        if line.startswith('## 9. '): add_diagram('domain.png','Figure 2 — Modèle de données et associations.')
        if line.startswith('## 15. '): add_diagram('reservation-sequence.png','Figure 3 — Séquence simplifiée de réservation et confirmation.')
    elif line.startswith('### '): doc.add_heading(line[4:],level=3)
    elif line.startswith('> '):
        p=doc.add_paragraph(style='Quote'); inline(p,line[2:])
    elif re.match(r'^\d+\. ',line):
        p=doc.add_paragraph(style='List Number'); inline(p,re.sub(r'^\d+\. ','',line))
    elif line.startswith('- '):
        p=doc.add_paragraph(style='List Bullet'); inline(p,line[2:])
    else:
        p=doc.add_paragraph(); inline(p,line)
    i+=1
# Set core properties
doc.core_properties.title='Casamance Tourisme — Rapport de projet'
doc.core_properties.subject='Rapport académique Licence 3 Informatique'
doc.core_properties.author='Projet Casamance Tourisme'
doc.save(OUT)
print(OUT)
