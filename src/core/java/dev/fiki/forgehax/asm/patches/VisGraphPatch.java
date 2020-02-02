package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.transforming.ClassTransformer;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import java.util.Objects;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

public class VisGraphPatch extends ClassTransformer {
  
  public VisGraphPatch() {
    super(TypesMc.Classes.VisGraph);
  }
  
  @RegisterMethodTransformer
  private class SetOpaqueCube extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.VisGraph_setOpaqueCube;
    }
    
    @Inject(value = "Add hook at the end that can override the return value")
    public void inject(MethodNode main) {
      AbstractInsnNode top = main.instructions.getFirst();
      AbstractInsnNode bottom =
        ASMHelper.findPattern(main.instructions.getFirst(), new int[]{RETURN}, "x");
      
      Objects.requireNonNull(top, "Find pattern failed for top");
      Objects.requireNonNull(bottom, "Find pattern failed for bottom");
      
      LabelNode cancelNode = new LabelNode();
      
      InsnList insnList = new InsnList();
      insnList.add(
        ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldDisableCaveCulling));
      insnList.add(new JumpInsnNode(IFNE, cancelNode));
      
      main.instructions.insertBefore(top, insnList);
      main.instructions.insertBefore(bottom, cancelNode);
    }
  }
  
  @RegisterMethodTransformer
  private class ComputeVisibility extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.VisGraph_computeVisibility;
    }
    
    @Inject(
      value =
        "Add hook that adds or logic to the jump that checks if setAllVisible(true) should be called"
    )
    public void inject(MethodNode main) {
      AbstractInsnNode node =
        ASMHelper.findPattern(main.instructions.getFirst(), new int[]{SIPUSH, IF_ICMPGE}, "xx");
      
      Objects.requireNonNull(node, "Find pattern failed for node");
      
      // gets opcode IF_ICMPGE
      JumpInsnNode greaterThanJump = (JumpInsnNode) node.getNext();
      LabelNode nextIfStatement = greaterThanJump.label;
      LabelNode orLabel = new LabelNode();
      
      // remove IF_ICMPGE
      main.instructions.remove(greaterThanJump);
      
      InsnList insnList = new InsnList();
      insnList.add(new JumpInsnNode(IF_ICMPLT, orLabel));
      insnList.add(
        ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldDisableCaveCulling));
      insnList.add(new JumpInsnNode(IFEQ, nextIfStatement));
      insnList.add(orLabel);
      
      main.instructions.insert(node, insnList);
    }
  }
}