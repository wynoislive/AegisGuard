import React, { useRef } from 'react';
import { Canvas, useFrame, useLoader } from '@react-three/fiber';
import { Edges, useGLTF, Float, Center, Resize, useProgress, Html } from '@react-three/drei';
import * as THREE from 'three';

// Import the images directly to ensure Vite includes them in the build bundle correctly
import diamondOreImg from '../../public/blocks/diamond_ore.png';
import redstoneOreImg from '../../public/blocks/redstone_ore.png';
import bedrockImg from '../../public/blocks/bedrock.png';
import ancientDebrisImg from '../../public/blocks/ancient_debris.png';

function MobModel({ url, position, rotation, scaleMultiplier = 1 }) {
  const { scene } = useGLTF(url);
  // We use <Resize> to aggressively squash the model into a standard maximum 10x10 cube, 
  // and <Center> to align it perfectly to its local origin regardless of Blockbench offset.
  return (
    <Float speed={1.5} rotationIntensity={0.2} floatIntensity={0.5}>
      <group position={position} rotation={rotation} scale={scaleMultiplier}>
        <Resize scale={10}>
          <Center>
            <primitive object={scene} />
          </Center>
        </Resize>
      </group>
    </Float>
  );
}

// -----------------------------------------------------
// LOADING SCREEN ENGINE
// -----------------------------------------------------
function DragonLoadingOverlay() {
  const { active, progress } = useProgress();
  const dragonUrl = "/AegisGuard/models/minecraft_rainbow_dragon.glb";
  const { scene } = useGLTF(dragonUrl);
  const meshRef = useRef();

  useFrame((state) => {
    if (meshRef.current) {
      meshRef.current.rotation.y += 0.05; // Spin the dragon rapidly
      // Levitate it smoothly
      meshRef.current.position.y = Math.sin(state.clock.elapsedTime * 3) * 2; 
    }
  });

  // If loading is complete, unmount the overlay.
  if (!active) return null;

  return (
    <Html center zIndexRange={[100, 100]}>
      <div style={{
        position: 'fixed', top: '-50vh', left: '-50vw', width: '100vw', height: '100vh',
        background: 'rgba(0, 0, 0, 0.95)', display: 'flex', flexDirection: 'column',
        alignItems: 'center', justifyContent: 'center', color: '#00FFFF',
        fontFamily: 'Orbitron, sans-serif', zIndex: 9999
      }}>
        {/* Render the actual spinning 3D Dragon via Canvas inside the HTML overlay! */}
        <div style={{ width: '400px', height: '400px' }}>
             <Canvas camera={{ position: [0, 0, 15], fov: 50 }}>
                <ambientLight intensity={1} />
                <pointLight position={[5, 5, 5]} intensity={10} color="#00FFFF" />
                <group ref={meshRef}>
                   <Resize scale={8}>
                     <Center>
                       <primitive object={scene} />
                     </Center>
                   </Resize>
                </group>
             </Canvas>
        </div>
        <h2 style={{ letterSpacing: '4px', marginTop: '20px', textTransform: 'uppercase' }}>
          {progress < 10 ? 'ESTABLISHING SECURE LINK...' : `SYNCING ASSETS: ${Math.round(progress)}%`}
        </h2>
        <div style={{
          width: '300px', height: '4px', background: '#333', marginTop: '15px', overflow: 'hidden'
        }}>
          <div style={{
            width: `${progress}%`, height: '100%', background: '#00FFFF', transition: 'width 0.2s'
          }}></div>
        </div>
      </div>
    </Html>
  );
}

function MinecraftBlock({ position, delay, texturePath, glowColor, scale = 1 }) {
  const meshRef = useRef();
  
  // Load texture and apply NearestFilter for pixel-art crispness!
  const texture = useLoader(THREE.TextureLoader, texturePath);
  texture.magFilter = THREE.NearestFilter;
  texture.minFilter = THREE.NearestFilter;

  useFrame((state) => {
    const time = state.clock.getElapsedTime();
    // Floating animation
    meshRef.current.position.y = position[1] + Math.sin(time + delay) * 0.4;
    // Slow rotation
    meshRef.current.rotation.x = time * 0.15;
    meshRef.current.rotation.y = time * 0.25;
  });

  return (
    <group position={position} ref={meshRef} scale={[scale, scale, scale]}>
      <mesh>
        <boxGeometry args={[1, 1, 1]} />
        <meshStandardMaterial map={texture} roughness={0.8} />
        {/* Glow edge surrounding the block to match Cyberpunk vibe */}
        <Edges scale={1.02} threshold={15} color={glowColor} />
      </mesh>
      
      {/* Small PointLight attached to the block if it glows */}
      {glowColor !== "#000000" && (
        <pointLight color={glowColor} distance={3} intensity={5} />
      )}
    </group>
  );
}

function GridBackground() {
  return (
    <group position={[0, -3, -8]} rotation={[Math.PI / 2, 0, 0]}>
      {/* High tech neon grid at the bottom */}
      <gridHelper args={[60, 60, '#FF0000', '#220000']} />
    </group>
  );
}

export default function Background3D() {
  // Generate 25 randomized Minecraft blocks scattered across the 3D void
  const blocks = React.useMemo(() => {
    const textures = [
      { path: diamondOreImg, glow: "#00FFFF" },
      { path: ancientDebrisImg, glow: "#FF4400" },
      { path: bedrockImg, glow: "#000000" },
      { path: redstoneOreImg, glow: "#FF0000" }
    ];
    
    const calculatedBlocks = [];
    for (let i = 0; i < 80; i++) {
      // Widen the field immensely to cover the extremeties of ultra-wide monitors
      const x = (Math.random() - 0.5) * 50;
      const y = (Math.random() - 0.5) * 35;
      const z = -1 * (Math.random() * 15 + 2); // Push them varying distances into the Z-axis
      
      const delay = Math.random() * 5;
      const scale = 0.5 + Math.random() * 0.8; 
      const textureType = textures[Math.floor(Math.random() * textures.length)];

      calculatedBlocks.push({
        id: i,
        position: [x, y, z],
        delay,
        scale,
        texturePath: textureType.path,
        glowColor: textureType.glow
      });
    }
    return calculatedBlocks;
  }, []);

  return (
    <div style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', zIndex: -1 }}>
      {/* Set DPR bounds to 1.5 strictly to prevent Pixel Ratio scaling lag on 4K monitors */}
      <Canvas camera={{ position: [0, 0, 8], fov: 60 }} dpr={[1, 1.5]}>
        
        {/* The loading Screen mounts first and monitors Suspense progress */}
        <React.Suspense fallback={null}>
            <DragonLoadingOverlay />
        </React.Suspense>

        {/* Global ambient lights replace individual model PointLights for insane performance gains */}
        <ambientLight intensity={0.6} color="#aaccff" />
        <pointLight position={[10, 20, 10]} intensity={15} color="#ffffff" />
        <directionalLight position={[-10, 10, -5]} intensity={2} color="#00FFFF" />
        <directionalLight position={[10, -10, 5]} intensity={2} color="#FF0000" />
        
        {blocks.map((b) => (
          <MinecraftBlock 
            key={b.id} 
            position={b.position} 
            delay={b.delay} 
            texturePath={b.texturePath} 
            glowColor={b.glowColor} 
            scale={b.scale} 
          />
        ))}

        {/* 
            Separate individual Suspense boundaries for models so they stream in asynchronously 
            without completely freezing the main Canvas thread! 
        */}
        <React.Suspense fallback={null}><MobModel url="/AegisGuard/models/realistic_dragon_minecraft.glb" position={[-18, 5, -18]} scaleMultiplier={1.8} rotation={[0, 0.8, 0]} /></React.Suspense>
        <React.Suspense fallback={null}><MobModel url="/AegisGuard/models/minecraft_rainbow_dragon.glb" position={[18, 10, -22]} scaleMultiplier={2} rotation={[0, -0.6, 0.2]} /></React.Suspense>
        
        <React.Suspense fallback={null}><MobModel url="/AegisGuard/models/minecraft_warden.glb" position={[16, -6, -15]} scaleMultiplier={1.5} rotation={[0, -0.7, 0]} /></React.Suspense>
        <React.Suspense fallback={null}><MobModel url="/AegisGuard/models/minecraft_creeper.glb" position={[-10, -7, -9]} scaleMultiplier={1} rotation={[0, 0.5, 0]} /></React.Suspense>
        <React.Suspense fallback={null}><MobModel url="/AegisGuard/models/minecraft_villager_animatable.glb" position={[12, -7, -8]} scaleMultiplier={1.2} rotation={[0, -0.4, 0]} /></React.Suspense>
        <React.Suspense fallback={null}><MobModel url="/AegisGuard/models/minecraft_-_witch.glb" position={[-18, -4, -14]} scaleMultiplier={1.3} rotation={[0, 0.4, 0]} /></React.Suspense>
        
        <React.Suspense fallback={null}><MobModel url="/AegisGuard/models/minecraft_-_phantom.glb" position={[0, 15, -18]} scaleMultiplier={1.8} rotation={[0.4, 0, 0]} /></React.Suspense>
        
        <GridBackground />
      </Canvas>
      <div className="scanlines"></div>
    </div>
  );
}
