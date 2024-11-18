<?php

// autoload_real.php @generated by Composer

class ComposerAutoloaderInitb33d6c8910b9889895f9f3b0e362a5ed
{
    private static $loader;

    public static function loadClassLoader($class)
    {
        if ('Composer\Autoload\ClassLoader' === $class) {
            require __DIR__ . '/ClassLoader.php';
        }
    }

    /**
     * @return \Composer\Autoload\ClassLoader
     */
    public static function getLoader()
    {
        if (null !== self::$loader) {
            return self::$loader;
        }

        require __DIR__ . '/platform_check.php';

        spl_autoload_register(array('ComposerAutoloaderInitb33d6c8910b9889895f9f3b0e362a5ed', 'loadClassLoader'), true, true);
        self::$loader = $loader = new \Composer\Autoload\ClassLoader(\dirname(__DIR__));
        spl_autoload_unregister(array('ComposerAutoloaderInitb33d6c8910b9889895f9f3b0e362a5ed', 'loadClassLoader'));

        require __DIR__ . '/autoload_static.php';
        call_user_func(\Composer\Autoload\ComposerStaticInitb33d6c8910b9889895f9f3b0e362a5ed::getInitializer($loader));

        $loader->register(true);

        return $loader;
    }
}
