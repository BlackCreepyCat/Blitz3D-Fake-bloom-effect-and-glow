;GLOW EFFECT
;SSwifts blur routine ---------------------
; This is the location in the world where the camera will reside.
; This location should be far from any other geometry you have in your world.
Const BLUR_CAM_X# = 65536.0
Const BLUR_CAM_Y# = 65536.0
Const BLUR_CAM_Z# = 0.0
Global BLUR_CAM% = 0

Global GLsprite%

;NOTE!
;IF YOU ADJUST THE TEX SIZE YOU HAVE TO MESS WITH POSITION 
;VALUES INSIDE BLUR TEXTURE FUNCTION TO GET THE BLUR POSITIONED RIGHT.

Global GLtexsize% = 256
Global GLcam%
Global GLtex%
Global GLdummytex%

Dim BLURMESH%(4*4)

;Modified to additive blend in the same time as blur.
;Also modified the copyrecting... so that multiple passes don't cause texture drifting.
;this is just a quick fix... don't actually get why tex texture drifts...
Function BlurTexture(texture, Blur_Quality, Blur_Radius#, blurpasses%)
; -------------------------------------------------------------------------------------------------------------------
; This function blurs a texture using a technique that takes advantage of 3D acceleration.  
;
; * You MUST hide all other cameras before calling this function!
; * You MUST reset your texture's blending mode, scale, and position after calling this function!
;
; Texture is the texture you want blurred.
;
; Blur_Quality defines the quality of the blur.  1 = 4 passes, 2 = 8 passes, 3 = 12 passes, etc.
;
; (The reason that the passes are in multiples of four is because interference artifacts are created when
; the number of passes is not a multiple of four... meaning that ten passes will actually look far worse
; than eight.)
;
; Blur_Radius# defines the radius of the blur, in pixels, assuming a map size of 256x256.
;
;(Ie, a radius of 16 will be the same width regardless of whether the texture is 16x16 or 512x512.  It will
; only be exactly 16 pixels wide if the map is 256x256.)
; -------------------------------------------------------------------------------------------------------------------

; This is used for temporary storage of the meshes used for soft shadow blurring.
;Local BlurMesh[16*4]


; If blurring is enabled...
If Blur_Quality > 0

If BLUR_CAM=0
Blur_Cam = CreateCamera()

; Set the camera's range to be very small so as to reduce the possiblity of extra objects making it into the scene.
CameraRange Blur_Cam, 0.1, 100

; Set the camera to zoom in on the object to reduce perspective error from the object being too close to the camera.
CameraZoom Blur_Cam, 16.0

; Aim camera straight down.
RotateEntity Blur_Cam, 90, 0, 0, True

; Set the camera viewport to the same size as the texture.
CameraViewport Blur_Cam, 0, 0, GLtexsize, GLtexsize

; Set the camera so it clears the color buffer before rendering the texture.
CameraClsColor Blur_Cam, 0,0,0
CameraClsMode  Blur_Cam,True, True

; Position the blur camera far from other entities in the world.
PositionEntity Blur_Cam, BLUR_CAM_X#, BLUR_CAM_Y#, BLUR_CAM_Z#
Else
ShowEntity BLUR_CAM
End If

; Create the sprites to use for blurring the shadow maps.
For Loop = 0 To (Blur_Quality*4)-1

If BLURMESH(loop) = 0
BlurMesh(Loop) = CreateSprite()
EntityBlend BlurMesh(Loop),3;3

EntityTexture Blurmesh(loop),GLdummytex,0,0
EntityTexture BlurMesh(Loop),Texture,0,1
EntityTexture BlurMesh(loop),Texture,0,2

EntityFX BlurMesh(Loop), 1+8
ScaleSprite BlurMesh(Loop), 2, 2
;EntityAlpha Blurmesh(loop),0.5
End If

ShowEntity blurmesh(loop)
Next

; Scale the texture down because we scale the sprites up so they fill a larger area of the
; screen.  (Otherwise the edges of the texture are darker than the middle because they don't
; get covered.
ScaleTexture    Texture, 0.5, 0.5
PositionTexture Texture, 0.5, 0.5

; Blur texture by blitting semi-transparent copies of it on top of it.
BlurRadius# = Blur_Radius# * (1.0 / 256.0)
BlurAngleStep# = 360.0 / Float(Blur_Quality*4)

; Normally we would just divide 255 by the number of passes so that adding all the passes
; together would not exceed 256.  However, if we did that, then we could not have a number of
; passes which does not divide 256 evenly, or else the error would result in the white part of
; the image being slightly less than white.  So we round partial values up to ensure that
; white will always be white, even if it ends up being a little whiter than white as a result
; when all the colors are added, since going higher than white just clamps to white.
;BlurShade = Ceil(255.0 / Float(Blur_Quality*4))

; Place each of the blur objects around a circle of radius blur_radius.
For Loop = 0 To (Blur_Quality*4)-1

BlurAngle# = BlurAngleStep# * Float(Loop) + 180.0*(Loop Mod 2)
Xoff# = BlurRadius# * Cos(BlurAngle#)
Yoff# = BlurRadius# * Sin(BlurAngle#)


;MESS WITH THESE VALUES IF YOU ADJUST THE TEXTURE SIZE
PositionEntity BlurMesh(Loop), BLUR_CAM_X# + Xoff#-0.006, BLUR_CAM_Y# - 16.0, BLUR_CAM_Z# + Yoff#+0.008 , True
Next

; Render the new texture.

For i=1 To blurpasses
RenderWorld
CopyRect 0, 0, TextureWidth(Texture), TextureHeight(Texture), 0, 0, BackBuffer(), TextureBuffer(Texture)
Next


; Free the blur entities.
For Loop = 0 To (Blur_Quality*4)-1
HideEntity Blurmesh(loop)
Next

; Free the blur camera.
HideEntity BLUR_CAM
EndIf
End Function




;Params
;CAM - Your main scene camera
Function InitGlow(cam)
GLcam = cam

If GLtex = 0
ClearTextureFilters()
GLtex = CreateTexture(GLtexsize,GLtexsize,1+16+32+256)
TextureFilter("",1+8)
End If

;CREATE SPRITE
GLsprite = CreateSprite()
EntityTexture GLsprite,GLtex
PositionEntity GLsprite,0,0,100
EntityOrder GLsprite,-9999
EntityBlend GLsprite,3
ScaleSprite GLsprite,100,100
EntityParent GLsprite,cam
EntityFX GLsprite,1

If gldummytex=0
ClearTextureFilters()
gldummytex = CreateTexture(32,32,1)
TextureFilter("",1+8)
TextureBlend gldummytex,2
SetGlowIntensity()
End If



;Test code
;Use this test code to check how the blur is aligned 
;sprite = CreateSprite()
;PositionEntity sprite,0,0,20
;ScaleSprite sprite,4,4
;EntityParent sprite,cam
;EntityOrder sprite,-9000
End Function

;Intensity Between 0-1 (between no glow to ---> my eyes hurt glow)
Function SetGlowIntensity(d#=0.2)
SetBuffer TextureBuffer(GLdummytex)
ClsColor 255*d,255*d,255*d
Cls
SetBuffer BackBuffer()
End Function


;Play with bleed to get desired result.... it's a bit difficult to explain... the higher value .. the more the glow  colours bleed
;Use tween as a tween value if you use render tweening in your app
;More blurpasses really slow things down so beware and makes the blur act differently
Function RenderGlow(bleed%=30,tween%=0,blurpasses%=1)
TextureBlend GLtex,5
EntityColor GLsprite,bleed,bleed,bleed

CameraViewport GLcam,0,0,GLtexsize,GLtexsize

RenderWorld tween
CopyRect 0,0,GLtexsize,GLtexsize,0,0,BackBuffer(),TextureBuffer(GLtex)

CameraViewport GLcam,0,0,GraphicsWidth(),GraphicsHeight()
EntityColor GLsprite,255,255,255


;TextureBlend GLtex,5
blurtexture(GLtex,1,4,blurpasses)
ScaleTexture GLtex, 1, 1
PositionTexture GLtex,0,0
;TextureBlend GLtex,2
End Function


Function FreeGlow()
If GLsprite<>0
FreeEntity GLsprite : GLsprite=0
FreeTexture GLtex : GLtex=0
FreeTexture GLdummytex : GLdummytex=0
FreeEntity BLUR_CAM : BLUR_CAM=0

For i=1 To 4*4
If blurmesh(i)<>0 Then FreeEntity blurmesh(i) : blurmesh(i)=0
Next

End If
End Function



