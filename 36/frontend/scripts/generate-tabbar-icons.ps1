# 已迁移至 generate-rounded-icons.mjs（iconfont 圆润面性风格）
# 生成 TabBar 图标（81x81 PNG）
Add-Type -AssemblyName System.Drawing

$outDir = Join-Path $PSScriptRoot "..\static\tabbar"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$size = 81
$gray = [System.Drawing.Color]::FromArgb(255, 153, 153, 153)
$orange = [System.Drawing.Color]::FromArgb(255, 120, 185, 177)

function New-Bitmap($color) {
  $bmp = New-Object System.Drawing.Bitmap $size, $size
  $g = [System.Drawing.Graphics]::FromImage($bmp)
  $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
  $g.Clear([System.Drawing.Color]::Transparent)
  return @{ Bmp = $bmp; G = $g }
}

function Save-Png($bmp, $path) {
  $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
  $bmp.Dispose()
}

function Draw-Home($g, $color) {
  $brush = New-Object System.Drawing.SolidBrush $color
  $pen = New-Object System.Drawing.Pen $color, 3
  $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
  $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
  $pts = @(
    [System.Drawing.Point]::new(40, 14),
    [System.Drawing.Point]::new(62, 28),
    [System.Drawing.Point]::new(62, 52),
    [System.Drawing.Point]::new(40, 66),
    [System.Drawing.Point]::new(18, 52),
    [System.Drawing.Point]::new(18, 28)
  )
  $g.FillPolygon($brush, $pts)
  $g.DrawLine($pen, 32, 42, 48, 42)
  $g.DrawLine($pen, 36, 48, 44, 48)
  $brush.Dispose()
  $pen.Dispose()
}

function Draw-Project($g, $color) {
  $brush = New-Object System.Drawing.SolidBrush $color
  $pen = New-Object System.Drawing.Pen $color, 2.5
  $g.FillRectangle($brush, 20, 26, 41, 32)
  $g.DrawRectangle($pen, 20, 26, 41, 32)
  $g.FillRectangle($brush, 26, 20, 29, 8)
  $g.DrawLine($pen, 26, 28, 26, 20)
  $g.DrawLine($pen, 55, 28, 55, 20)
  $g.FillRectangle($brush, 34, 38, 13, 3)
  $brush.Dispose()
  $pen.Dispose()
}

function Draw-Activity($g, $color) {
  $brush = New-Object System.Drawing.SolidBrush $color
  $pen = New-Object System.Drawing.Pen $color, 2.5
  $g.DrawRectangle($pen, 22, 22, 37, 40)
  $g.DrawLine($pen, 22, 32, 59, 32)
  $g.DrawLine($pen, 32, 22, 32, 32)
  $g.DrawLine($pen, 42, 22, 42, 28)
  $g.DrawLine($pen, 50, 22, 50, 28)
  $points = @(
    [System.Drawing.Point]::new(28, 42),
    [System.Drawing.Point]::new(40, 52),
    [System.Drawing.Point]::new(52, 38)
  )
  $g.DrawLines($pen, $points)
  $brush.Dispose()
  $pen.Dispose()
}

function Draw-Mine($g, $color) {
  $brush = New-Object System.Drawing.SolidBrush $color
  $g.FillEllipse($brush, 30, 18, 22, 22)
  $path = New-Object System.Drawing.Drawing2D.GraphicsPath
  $path.AddArc(14, 40, 53, 36, 180, 180)
  $g.FillPath($brush, $path)
  $brush.Dispose()
}

$icons = @(
  @{ Name = "home"; Draw = ${function:Draw-Home} },
  @{ Name = "project"; Draw = ${function:Draw-Project} },
  @{ Name = "activity"; Draw = ${function:Draw-Activity} },
  @{ Name = "mine"; Draw = ${function:Draw-Mine} }
)

foreach ($icon in $icons) {
  $ctx = New-Bitmap $gray
  & $icon.Draw $ctx.G $gray
  Save-Png $ctx.Bmp (Join-Path $outDir "$($icon.Name).png")

  $ctx2 = New-Bitmap $orange
  & $icon.Draw $ctx2.G $orange
  Save-Png $ctx2.Bmp (Join-Path $outDir "$($icon.Name)-active.png")
}

Write-Host "TabBar icons generated in $outDir"
