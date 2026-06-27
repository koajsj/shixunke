$ErrorActionPreference = 'Stop'

function S {
    param([int[]]$Codes)
    return -join ($Codes | ForEach-Object { [char]$_ })
}

function New-VisioApp {
    $app = New-Object -ComObject Visio.Application
    $app.Visible = $false
    return $app
}

function Set-ShapeText {
    param($Shape, [string]$Text)
    $Shape.Text = $Text
    try {
        $Shape.CellsU("Char.Size").ResultIU = 0.18
        $Shape.CellsU("Para.HorzAlign").FormulaU = "0"
    } catch {
    }
}

function New-Box {
    param($Page, [double]$X1, [double]$Y1, [double]$X2, [double]$Y2, [string]$Text)
    $shape = $Page.DrawRectangle($X1, $Y1, $X2, $Y2)
    $shape.CellsU("FillForegnd").FormulaU = "RGB(255,255,255)"
    $shape.CellsU("LineWeight").ResultIU = 0.013
    Set-ShapeText -Shape $shape -Text $Text
    return $shape
}

function New-Ellipse {
    param($Page, [double]$X1, [double]$Y1, [double]$X2, [double]$Y2, [string]$Text)
    $shape = $Page.DrawOval($X1, $Y1, $X2, $Y2)
    $shape.CellsU("FillForegnd").FormulaU = "RGB(255,255,255)"
    $shape.CellsU("LineWeight").ResultIU = 0.013
    Set-ShapeText -Shape $shape -Text $Text
    return $shape
}

function New-Line {
    param($Page, [double]$X1, [double]$Y1, [double]$X2, [double]$Y2)
    $line = $Page.DrawLine($X1, $Y1, $X2, $Y2)
    $line.CellsU("LineWeight").ResultIU = 0.01
    return $line
}

function New-LabeledLine {
    param($Page, [double]$X1, [double]$Y1, [double]$X2, [double]$Y2, [string]$Text)
    $line = New-Line -Page $Page -X1 $X1 -Y1 $Y1 -X2 $X2 -Y2 $Y2
    $label = $Page.DrawRectangle((($X1 + $X2) / 2) - 0.7, (($Y1 + $Y2) / 2) - 0.15, (($X1 + $X2) / 2) + 0.7, (($Y1 + $Y2) / 2) + 0.15)
    $label.CellsU("LinePattern").FormulaU = "0"
    $label.CellsU("FillPattern").FormulaU = "0"
    Set-ShapeText -Shape $label -Text $Text
    return $line
}

function New-Actor {
    param($Page, [double]$CenterX, [double]$BaseY, [string]$Text)
    $head = $Page.DrawOval($CenterX - 0.18, $BaseY + 1.1, $CenterX + 0.18, $BaseY + 1.46)
    [void]$head
    [void](New-Line -Page $Page -X1 $CenterX -Y1 ($BaseY + 1.1) -X2 $CenterX -Y2 ($BaseY + 0.45))
    [void](New-Line -Page $Page -X1 ($CenterX - 0.35) -Y1 ($BaseY + 0.9) -X2 ($CenterX + 0.35) -Y2 ($BaseY + 0.9))
    [void](New-Line -Page $Page -X1 $CenterX -Y1 ($BaseY + 0.45) -X2 ($CenterX - 0.3) -Y2 $BaseY)
    [void](New-Line -Page $Page -X1 $CenterX -Y1 ($BaseY + 0.45) -X2 ($CenterX + 0.3) -Y2 $BaseY)
    $label = $Page.DrawRectangle($CenterX - 0.8, $BaseY - 0.45, $CenterX + 0.8, $BaseY - 0.05)
    $label.CellsU("LinePattern").FormulaU = "0"
    $label.CellsU("FillPattern").FormulaU = "0"
    Set-ShapeText -Shape $label -Text $Text
}

function Add-Title {
    param($Page, [string]$Text, [double]$Width)
    $title = $Page.DrawRectangle(0.4, 10.0, $Width - 0.4, 10.6)
    $title.CellsU("LinePattern").FormulaU = "0"
    $title.CellsU("FillPattern").FormulaU = "0"
    $title.CellsU("Char.Size").ResultIU = 0.24
    $title.CellsU("Para.HorzAlign").FormulaU = "1"
    $title.Text = $Text
}

function Save-Drawing {
    param($Document, [string]$Path)
    if (Test-Path $Path) {
        Remove-Item -LiteralPath $Path -Force
    }
    $Document.SaveAs($Path)
    $Document.Close()
}

$tUseCase = S 31199,36710,31649,29702,31995,32479,29992,20363,22270
$tAdmin = S 31649,29702,21592
$tUser = S 26222,36890,29992,25143
$tSystem = S 31199,36710,31649,29702,31995,32479
$tLogin = S 30331,24405,31995,32479
$tLogout = S 36864,20986,31995,32479
$tMe = S 26597,30475,24403,21069,30331,24405,20449,24687
$tCustomerMgmt = S 23458,25143,20449,24687,31649,29702
$tCarMgmt = S 36710,36742,20449,24687,31649,29702
$tRentalMgmt = S 31199,36161,20449,24687,31649,29702
$tPaymentMgmt = S 25903,20184,20449,24687,31649,29702

$tSequence = S 25903,20184,21019,24314,26102,24207,22270
$tPaymentController = S 25903,20184,25511,21046,22120
$tPaymentService = S 25903,20184,26381,21153
$tRentalRepo = S 31199,36161,20179,24211
$tPaymentRepo = S 25903,20184,20179,24211
$m1 = "1. " + (S 25552,20132,25903,20184,35831,27714)
$m2 = "2. " + (S 35843,29992,21019,24314,25903,20184)
$m3 = "3. " + (S 26597,35810,31199,36161,35760,24405)
$m4 = "4. " + (S 36820,22238,31199,36161,25968,25454)
$m5 = "5. " + (S 26657,39564,25903,20184,26465,20214)
$m6 = "6. " + (S 32479,35745,24050,26377,25903,20184,37329,39069)
$m7 = "7. " + (S 36820,22238,32047,35745,37329,39069)
$m8 = "8. " + (S 20445,23384,25903,20184,35760,24405)
$m9 = "9. " + (S 36820,22238,25903,20184,32467,26524)
$m10 = "10. " + (S 26356,26032,31199,36161,29366,24577)
$m11 = "11. " + (S 36820,22238,21019,24314,25104,21151)

$tArchitecture = S 31199,36710,31649,29702,31995,32479,26550,26500,22270
$tClient = S 35843,29992,26041,32,47,32,21069,31471
$tControllerLayer = S 25511,21046,23618
$tAuthController = S 35748,35777,25511,21046,22120
$tCustomerController = S 23458,25143,25511,21046,22120
$tCarController = S 36710,36742,25511,21046,22120
$tRentalController = S 31199,36161,25511,21046,22120
$tAuthLayer = S 35748,35777,19982,25318,25130
$tAuthInterceptor = S 35748,35777,25318,25130,22120,65288,25318,25130,32,47,97,112,105,47,42,42,65289
$tAuthService = S 35748,35777,26381,21153
$tServiceLayer = S 19994,21153,23618
$tCustomerService = S 23458,25143,26381,21153
$tCarService = S 36710,36742,26381,21153
$tRentalService = S 31199,36161,26381,21153
$tRepoLayer = S 25345,20037,23618
$tCustomerRepo = S 23458,25143,20179,24211
$tCarRepo = S 36710,36742,20179,24211
$tDatabase = S 20851,31995,22411,25968,25454,24211,65306,23458,25143,34920,12289,36710,36742,34920,12289,31199,36161,34920,12289,25903,20184,34920

$tEr = S 31199,36710,31649,29702,31995,32479,32,69,82,32,22270
$tCustomer = S 23458,25143
$tCustomerPk = S 23458,25143,32534,21495,65288,20027,38190,65289
$tName = S 22995,21517
$tPhone = S 30005,35805
$tEmail = S 37038,31665
$tLicense = S 39550,39542,35777,21495
$tRental = S 31199,36161
$tRentalPk = S 31199,36161,32534,21495,65288,20027,38190,65289
$tCustomerFk = S 23458,25143,32534,21495,65288,22806,38190,65289
$tCarFk = S 36710,36742,32534,21495,65288,22806,38190,65289
$tRentalDate = S 31199,36161,26085,26399
$tReturnDate = S 24212,36824,26085,26399
$tActualReturnDate = S 23454,38469,24402,36824,26085,26399
$tTotal = S 24635,37329,39069
$tStatus = S 29366,24577
$tCar = S 36710,36742
$tCarPk = S 36710,36742,32534,21495,65288,20027,38190,65289
$tPlate = S 36710,29260,21495
$tBrand = S 21697,29260
$tModel = S 22411,21495
$tColor = S 39068,33394
$tYear = S 24180,20221
$tDaily = S 26085,31199,37329
$tPayment = S 25903,20184
$tPaymentPk = S 25903,20184,32534,21495,65288,20027,38190,65289
$tRentalFk = S 31199,36161,32534,21495,65288,22806,38190,65289
$tPaymentDate = S 25903,20184,26085,26399
$tPaymentAmount = S 25903,20184,37329,39069
$tPaymentMethod = S 25903,20184,26041,24335
$tOneToMany = S 49,32,23545,22810
$tPaymentTable = S 20851,31995,22411,25968,25454,24211,65306,23458,25143,34920,12289,36710,36742,34920,12289,31199,36161,34920,12289,25903,20184,34920

function New-UseCaseDiagram {
    param($App, [string]$Path)
    $doc = $App.Documents.Add("")
    $page = $doc.Pages.Item(1)
    $page.PageSheet.CellsU("PageWidth").ResultIU = 13
    $page.PageSheet.CellsU("PageHeight").ResultIU = 11
    Add-Title -Page $page -Text $tUseCase -Width 13
    New-Actor -Page $page -CenterX 1.5 -BaseY 5.1 -Text $tAdmin
    New-Actor -Page $page -CenterX 11.5 -BaseY 5.1 -Text $tUser
    $system = New-Box -Page $page -X1 3.0 -Y1 1.1 -X2 10.0 -Y2 9.4 -Text $tSystem
    $system.CellsU("Char.Size").ResultIU = 0.22
    $items = @(
        @{ y = 8.2; text = $tLogin },
        @{ y = 7.0; text = $tLogout },
        @{ y = 5.8; text = $tMe },
        @{ y = 4.6; text = $tCustomerMgmt },
        @{ y = 3.4; text = $tCarMgmt },
        @{ y = 2.2; text = $tRentalMgmt },
        @{ y = 1.4; text = $tPaymentMgmt }
    )
    foreach ($item in $items) {
        [void](New-Ellipse -Page $page -X1 4.4 -Y1 ($item.y - 0.35) -X2 8.6 -Y2 ($item.y + 0.35) -Text $item.text)
    }
    foreach ($y in 8.2, 7.0, 5.8, 4.6, 3.4, 2.2, 1.4) {
        [void](New-Line -Page $page -X1 2.0 -Y1 5.8 -X2 4.4 -Y2 $y)
    }
    foreach ($y in 8.2, 7.0, 5.8) {
        [void](New-Line -Page $page -X1 11.0 -Y1 5.8 -X2 8.6 -Y2 $y)
    }
    Save-Drawing -Document $doc -Path $Path
}

function New-SequenceDiagram {
    param($App, [string]$Path)
    $doc = $App.Documents.Add("")
    $page = $doc.Pages.Item(1)
    $page.PageSheet.CellsU("PageWidth").ResultIU = 13
    $page.PageSheet.CellsU("PageHeight").ResultIU = 11
    Add-Title -Page $page -Text $tSequence -Width 13
    $lanes = @(
        @{ x = 1.2; text = $tAdmin },
        @{ x = 3.8; text = $tPaymentController },
        @{ x = 6.4; text = $tPaymentService },
        @{ x = 9.0; text = $tRentalRepo },
        @{ x = 11.6; text = $tPaymentRepo }
    )
    foreach ($lane in $lanes) {
        [void](New-Box -Page $page -X1 ($lane.x - 0.8) -Y1 9.0 -X2 ($lane.x + 0.8) -Y2 9.6 -Text $lane.text)
        $life = New-Line -Page $page -X1 $lane.x -Y1 8.9 -X2 $lane.x -Y2 1.1
        $life.CellsU("LinePattern").FormulaU = "2"
    }
    [void](New-LabeledLine -Page $page -X1 1.2 -Y1 8.2 -X2 3.8 -Y2 8.2 -Text $m1)
    [void](New-LabeledLine -Page $page -X1 3.8 -Y1 7.3 -X2 6.4 -Y2 7.3 -Text $m2)
    [void](New-LabeledLine -Page $page -X1 6.4 -Y1 6.4 -X2 9.0 -Y2 6.4 -Text $m3)
    [void](New-LabeledLine -Page $page -X1 9.0 -Y1 5.8 -X2 6.4 -Y2 5.8 -Text $m4)
    [void](New-Box -Page $page -X1 5.6 -Y1 5.0 -X2 7.2 -Y2 5.45 -Text $m5)
    [void](New-LabeledLine -Page $page -X1 6.4 -Y1 4.5 -X2 11.6 -Y2 4.5 -Text $m6)
    [void](New-LabeledLine -Page $page -X1 11.6 -Y1 3.9 -X2 6.4 -Y2 3.9 -Text $m7)
    [void](New-LabeledLine -Page $page -X1 6.4 -Y1 3.1 -X2 11.6 -Y2 3.1 -Text $m8)
    [void](New-LabeledLine -Page $page -X1 11.6 -Y1 2.5 -X2 6.4 -Y2 2.5 -Text $m9)
    [void](New-LabeledLine -Page $page -X1 6.4 -Y1 1.8 -X2 9.0 -Y2 1.8 -Text $m10)
    [void](New-LabeledLine -Page $page -X1 6.4 -Y1 1.3 -X2 3.8 -Y2 1.3 -Text $m11)
    Save-Drawing -Document $doc -Path $Path
}

function New-ArchitectureDiagram {
    param($App, [string]$Path)
    $doc = $App.Documents.Add("")
    $page = $doc.Pages.Item(1)
    $page.PageSheet.CellsU("PageWidth").ResultIU = 13
    $page.PageSheet.CellsU("PageHeight").ResultIU = 11
    Add-Title -Page $page -Text $tArchitecture -Width 13
    [void](New-Box -Page $page -X1 4.7 -Y1 9.0 -X2 8.3 -Y2 9.7 -Text $tClient)
    [void](New-Box -Page $page -X1 1.0 -Y1 7.2 -X2 12.0 -Y2 8.5 -Text $tControllerLayer)
    [void](New-Box -Page $page -X1 1.3 -Y1 7.45 -X2 3.1 -Y2 8.1 -Text $tAuthController)
    [void](New-Box -Page $page -X1 3.4 -Y1 7.45 -X2 5.2 -Y2 8.1 -Text $tCustomerController)
    [void](New-Box -Page $page -X1 5.5 -Y1 7.45 -X2 7.3 -Y2 8.1 -Text $tCarController)
    [void](New-Box -Page $page -X1 7.6 -Y1 7.45 -X2 9.4 -Y2 8.1 -Text $tRentalController)
    [void](New-Box -Page $page -X1 9.7 -Y1 7.45 -X2 11.5 -Y2 8.1 -Text $tPaymentController)
    [void](New-Box -Page $page -X1 1.0 -Y1 5.8 -X2 12.0 -Y2 6.8 -Text $tAuthLayer)
    [void](New-Box -Page $page -X1 2.0 -Y1 6.0 -X2 5.2 -Y2 6.45 -Text $tAuthInterceptor)
    [void](New-Box -Page $page -X1 7.0 -Y1 6.0 -X2 10.2 -Y2 6.45 -Text $tAuthService)
    [void](New-Box -Page $page -X1 1.0 -Y1 4.1 -X2 12.0 -Y2 5.4 -Text $tServiceLayer)
    [void](New-Box -Page $page -X1 1.3 -Y1 4.35 -X2 3.7 -Y2 5.0 -Text $tCustomerService)
    [void](New-Box -Page $page -X1 4.0 -Y1 4.35 -X2 6.4 -Y2 5.0 -Text $tCarService)
    [void](New-Box -Page $page -X1 6.7 -Y1 4.35 -X2 9.1 -Y2 5.0 -Text $tRentalService)
    [void](New-Box -Page $page -X1 9.4 -Y1 4.35 -X2 11.8 -Y2 5.0 -Text $tPaymentService)
    [void](New-Box -Page $page -X1 1.0 -Y1 2.3 -X2 12.0 -Y2 3.6 -Text $tRepoLayer)
    [void](New-Box -Page $page -X1 1.3 -Y1 2.55 -X2 3.7 -Y2 3.2 -Text $tCustomerRepo)
    [void](New-Box -Page $page -X1 4.0 -Y1 2.55 -X2 6.4 -Y2 3.2 -Text $tCarRepo)
    [void](New-Box -Page $page -X1 6.7 -Y1 2.55 -X2 9.1 -Y2 3.2 -Text $tRentalRepo)
    [void](New-Box -Page $page -X1 9.4 -Y1 2.55 -X2 11.8 -Y2 3.2 -Text $tPaymentRepo)
    [void](New-Box -Page $page -X1 2.5 -Y1 0.8 -X2 10.5 -Y2 1.8 -Text $tDatabase)
    foreach ($x in 2.2, 4.3, 6.4, 8.5, 10.6) {
        [void](New-Line -Page $page -X1 $x -Y1 8.95 -X2 $x -Y2 8.5)
    }
    foreach ($x in 2.6, 8.6) {
        [void](New-Line -Page $page -X1 $x -Y1 7.2 -X2 $x -Y2 6.8)
    }
    foreach ($x in 2.5, 5.2, 7.9, 10.6) {
        [void](New-Line -Page $page -X1 $x -Y1 5.8 -X2 $x -Y2 5.4)
        [void](New-Line -Page $page -X1 $x -Y1 4.1 -X2 $x -Y2 3.6)
    }
    [void](New-Line -Page $page -X1 6.5 -Y1 2.3 -X2 6.5 -Y2 1.8)
    Save-Drawing -Document $doc -Path $Path
}

function New-ErDiagram {
    param($App, [string]$Path)
    $doc = $App.Documents.Add("")
    $page = $doc.Pages.Item(1)
    $page.PageSheet.CellsU("PageWidth").ResultIU = 13
    $page.PageSheet.CellsU("PageHeight").ResultIU = 11
    Add-Title -Page $page -Text $tEr -Width 13
    $customerText = $tCustomer + "`n----------------`n" + $tCustomerPk + "`n" + $tName + "`n" + $tPhone + "`n" + $tEmail + "`n" + $tLicense
    $rentalText = $tRental + "`n----------------`n" + $tRentalPk + "`n" + $tCustomerFk + "`n" + $tCarFk + "`n" + $tRentalDate + "`n" + $tReturnDate + "`n" + $tActualReturnDate + "`n" + $tTotal + "`n" + $tStatus
    $carText = $tCar + "`n----------------`n" + $tCarPk + "`n" + $tPlate + "`n" + $tBrand + "`n" + $tModel + "`n" + $tColor + "`n" + $tYear + "`n" + $tStatus + "`n" + $tDaily
    $paymentText = $tPayment + "`n----------------`n" + $tPaymentPk + "`n" + $tRentalFk + "`n" + $tPaymentDate + "`n" + $tPaymentAmount + "`n" + $tPaymentMethod
    [void](New-Box -Page $page -X1 0.8 -Y1 5.8 -X2 3.6 -Y2 8.7 -Text $customerText)
    [void](New-Box -Page $page -X1 5.0 -Y1 5.2 -X2 8.2 -Y2 8.9 -Text $rentalText)
    [void](New-Box -Page $page -X1 9.2 -Y1 5.8 -X2 12.0 -Y2 8.7 -Text $carText)
    [void](New-Box -Page $page -X1 5.0 -Y1 1.4 -X2 8.2 -Y2 4.2 -Text $paymentText)
    [void](New-Line -Page $page -X1 3.6 -Y1 7.2 -X2 5.0 -Y2 7.2)
    [void](New-Line -Page $page -X1 8.2 -Y1 7.2 -X2 9.2 -Y2 7.2)
    [void](New-Line -Page $page -X1 6.6 -Y1 5.2 -X2 6.6 -Y2 4.2)
    [void](New-Box -Page $page -X1 4.0 -Y1 7.0 -X2 4.8 -Y2 7.4 -Text $tOneToMany)
    [void](New-Box -Page $page -X1 8.3 -Y1 7.0 -X2 9.1 -Y2 7.4 -Text $tOneToMany)
    [void](New-Box -Page $page -X1 6.0 -Y1 4.5 -X2 7.2 -Y2 4.9 -Text $tOneToMany)
    Save-Drawing -Document $doc -Path $Path
}

$tempDir = "C:\temp\visio-output"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

$app = $null
try {
    $app = New-VisioApp
    New-UseCaseDiagram -App $app -Path (Join-Path $tempDir "usecase.vsdx")
    New-SequenceDiagram -App $app -Path (Join-Path $tempDir "sequence.vsdx")
    New-ArchitectureDiagram -App $app -Path (Join-Path $tempDir "architecture.vsdx")
    New-ErDiagram -App $app -Path (Join-Path $tempDir "er.vsdx")
} finally {
    if ($app -ne $null) {
        $app.Quit()
        [System.Runtime.InteropServices.Marshal]::ReleaseComObject($app) | Out-Null
    }
    [GC]::Collect()
    [GC]::WaitForPendingFinalizers()
}

Write-Output "done"
