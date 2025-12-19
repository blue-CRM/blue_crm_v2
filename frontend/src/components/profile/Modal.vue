<template>
  <Teleport to="body">
    <div class="fixed inset-0 z-[100000] flex items-center justify-center p-4">
      <!-- backdrop -->
      <div
          class="absolute inset-0 bg-black/40 dark:border-gray-800"
          aria-hidden="true"
          @click="$emit('close')"
      ></div>

      <!-- content -->
      <div class="relative z-[100001] w-full max-w-[700px]">
        <slot name="body"></slot>
      </div>
    </div>
  </Teleport>
</template>
