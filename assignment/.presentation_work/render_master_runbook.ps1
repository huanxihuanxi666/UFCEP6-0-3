$word = New-Object -ComObject Word.Application
$word.Visible = $false
$docx = (Resolve-Path 'output/总图三条路径_Tasklist填写运行手册.docx').Path
$pdf = Join-Path (Resolve-Path '.presentation_work').Path 'master_runbook.pdf'
$doc = $word.Documents.Open($docx)
$doc.SaveAs2($pdf, 17)
$doc.Close()
$word.Quit()
Remove-Item -LiteralPath '.presentation_work/master_runbook_pages' -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path '.presentation_work/master_runbook_pages' | Out-Null
& pdftoppm -png -r 100 '.presentation_work/master_runbook.pdf' '.presentation_work/master_runbook_pages/page'
