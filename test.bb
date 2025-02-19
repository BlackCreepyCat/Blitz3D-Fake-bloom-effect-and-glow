;Bloomtest
Graphics3D 800,600,32,2


;FPS CALCULATOR
;----------------------------------------------------------------------------
Global framecounter_counter
Global framecounter_time
Global framecounter_framerate
Global FPS$
Function FPS$()
    framecounter_counter=framecounter_counter+1
    If framecounter_time=0 Then framecounter_time=MilliSecs()
    If framecounter_time+1001<MilliSecs() Then
        framecounter_framerate=framecounter_counter
           framecounter_counter=0
        framecounter_time=MilliSecs()
        EndIf
        zsemi$="FPS: "+framecounter_framerate
Return zsemi$
End Function
;---------------------------------------------------------------------------



;INCLUDE BLOOMFILTER
Include "Bloomfilter.bb"


;CREATE CAMERA & LIGHT
cam = CreateCamera()
light = CreateLight()

;CREATE TEAPOT
mesh = LoadMesh("teapot.x")
tex = LoadTexture("spheremap.bmp",64)
EntityTexture mesh,tex
PositionEntity mesh,0,0,3

;INITIALIZE GLOW
InitGlow(cam)

Glowint# = 0.15
GlowBleed# = 30

Repeat
TurnEntity mesh,0,0.2,0.2


If MouseDown(1) = 1
TranslateEntity mesh,0,0,0.1
End If
If MouseDown(2) = 1
TranslateEntity mesh,0,0,-0.1
End If

If KeyDown(30) = 1 And Glowint<1 Then Glowint=Glowint+0.001 
If KeyDown(44) = 1 And glowint>0.01 Then Glowint=Glowint-0.001 
If KeyDown(31) = 1 Then Glowbleed=Glowbleed+0.1 
If KeyDown(45) = 1 Then Glowbleed=Glowbleed-0.1 

SetGlowIntensity(Glowint)
RenderGlow(Glowbleed)


RenderWorld
Text 0,0,FPS() 
Text 0,15,"Glow Intensity (a/z): " + Glowint
Text 0,30,"Glow Bleed (s/x): " + GlowBleed
Text 0,45,"Left/Right mouse to zoom in/out"

Flip True
Until KeyDown(1)=1
FreeGLow()

End






;~IDEal Editor Parameters:
;~C#Blitz3D