import React, { useRef } from 'react';
import { Canvas, useFrame, useLoader } from '@react-three/fiber';
import { Edges } from '@react-three/drei';
import * as THREE from 'three';

// Import the images directly to ensure Vite includes them in the build bundle correctly
import diamondOreImg from '../../public/blocks/diamond_ore.png';
import redstoneOreImg from '../../public/blocks/redstone_ore.png';
import bedrockImg from '../../public/blocks/bedrock.png';
import ancientDebrisImg from '../../public/blocks/ancient_debris.png';

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
  return (
    <div style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', zIndex: -1 }}>
      <Canvas camera={{ position: [0, 0, 8], fov: 60 }}>
        {/* Ambient lighting is low to emphasize the glow/cyberpunk aesthetic */}
        <ambientLight intensity={0.4} color="#aaccff" />
        <pointLight position={[10, 20, 10]} intensity={10} color="#ffffff" />
        
        <MinecraftBlock position={[-4, 1.5, -2]} delay={0} texturePath={diamondOreImg} glowColor="#00FFFF" scale={1.2} />
        <MinecraftBlock position={[4, -1, -3]} delay={2} texturePath={ancientDebrisImg} glowColor="#FF4400" scale={1.1} />
        <MinecraftBlock position={[-3, -2, -1]} delay={1} texturePath={bedrockImg} glowColor="#000000" scale={1} />
        <MinecraftBlock position={[3.5, 2.5, -4]} delay={3} texturePath={redstoneOreImg} glowColor="#FF0000" scale={1.3} />
        
        <GridBackground />
      </Canvas>
      <div className="scanlines"></div>
    </div>
  );
}
