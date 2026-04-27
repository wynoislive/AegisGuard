import React, { useRef } from 'react';
import { Canvas, useFrame, useLoader } from '@react-three/fiber';
import { Edges, useGLTF, Float } from '@react-three/drei';
import * as THREE from 'three';

// Import the images directly to ensure Vite includes them in the build bundle correctly
import diamondOreImg from '../../public/blocks/diamond_ore.png';
import redstoneOreImg from '../../public/blocks/redstone_ore.png';
import bedrockImg from '../../public/blocks/bedrock.png';
import ancientDebrisImg from '../../public/blocks/ancient_debris.png';

function MobModel({ url, position, scale, rotation, glowColor }) {
  // useGLTF relies on Suspense. It throws a Promise while loading. 
  // We must not catch it synchronously, otherwise React instantly abandons the render.
  const { scene } = useGLTF(url);
  return (
    <Float speed={2} rotationIntensity={0.5} floatIntensity={1}>
      <group position={position} scale={scale} rotation={rotation}>
        <primitive object={scene} />
        {glowColor && <pointLight color={glowColor} intensity={20} distance={10} />}
      </group>
    </Float>
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
      <Canvas camera={{ position: [0, 0, 8], fov: 60 }}>
        {/* Ambient lighting is low to emphasize the glow/cyberpunk aesthetic */}
        <ambientLight intensity={0.4} color="#aaccff" />
        <pointLight position={[10, 20, 10]} intensity={10} color="#ffffff" />
        
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

        {/* 3D Sketchfab Models (User Downloaded) */}
        <React.Suspense fallback={null}>
            {/* The Dragons soar high and massive in the deep background (Z layer -15 / -20) */}
            <MobModel url="/AegisGuard/models/realistic_dragon_minecraft.glb" position={[-18, 8, -18]} scale={1.2} rotation={[0, 0.8, 0]} glowColor="#FF0000" />
            <MobModel url="/AegisGuard/models/minecraft_rainbow_dragon.glb" position={[18, 10, -22]} scale={1.6} rotation={[0, -0.6, 0.2]} glowColor="#00FFFF" />
            
            {/* Ground troops spread wide along the X axis and varied depth */}
            <MobModel url="/AegisGuard/models/minecraft_warden.glb" position={[16, -3, -15]} scale={1.4} rotation={[0, -0.7, 0]} glowColor="#00FFFF" />
            <MobModel url="/AegisGuard/models/minecraft_creeper.glb" position={[-10, -4, -9]} scale={1.5} rotation={[0, 0.5, 0]} glowColor="#00FF00" />
            <MobModel url="/AegisGuard/models/minecraft_villager_animatable.glb" position={[12, -5, -8]} scale={1.4} rotation={[0, -0.4, 0]} glowColor="#55FF55" />
            <MobModel url="/AegisGuard/models/minecraft_-_witch.glb" position={[-18, -2, -14]} scale={1.3} rotation={[0, 0.4, 0]} glowColor="#AA00FF" />
            
            {/* Phantom lurking near the very top ceiling of the monitor */}
            <MobModel url="/AegisGuard/models/minecraft_-_phantom.glb" position={[0, 12, -18]} scale={2} rotation={[0.4, 0, 0]} glowColor="#5500FF" />
        </React.Suspense>
        
        <GridBackground />
      </Canvas>
      <div className="scanlines"></div>
    </div>
  );
}
