package com.cramsan.awslib

import com.cramsan.awslib.ai.implementation.DummyAIRepoImpl
import com.cramsan.awslib.dsl.scene
import com.cramsan.awslib.entity.implementation.ConsumableType
import com.cramsan.awslib.entitymanager.implementation.EntityManager
import com.cramsan.awslib.map.GameMap
import com.cramsan.awslib.utils.constants.InitialValues
import com.cramsan.awslib.utils.map.MapLoader
import com.cramsan.framework.assertlib.AssertUtil
import com.cramsan.framework.assertlib.implementation.AssertUtilImpl
import com.cramsan.framework.halt.implementation.HaltUtilImpl
import com.cramsan.framework.halt.implementation.HaltUtilJVM
import com.cramsan.framework.logging.EventLogger
import com.cramsan.framework.logging.Severity
import com.cramsan.framework.logging.implementation.EventLoggerImpl
import com.cramsan.framework.logging.implementation.LoggerJVM
import java.awt.EventQueue

class AWTRunner {

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            EventQueue.invokeLater(::createAndShowGUI)
        }

        private fun createAndShowGUI() {
            val eventLogger = EventLoggerImpl(Severity.VERBOSE, null, LoggerJVM())
            EventLogger.setInstance(eventLogger)
            val haltUtil = HaltUtilImpl(HaltUtilJVM())
            val assertUtil = AssertUtilImpl(true, eventLogger, haltUtil)
            AssertUtil.setInstance(assertUtil)

            val map = GameMap(MapLoader().loadCSVMap("map1.txt"))

            val sceneConfig = scene {
                player {
                    posX = 12
                    posY = 29
                    speed = 20
                }

                entityBuilders {
                    enemy {
                        id = "dog"
                    }
                    ally {
                        id = "scientist"
                    }
                }

                entity {
                    enemy {
                        id = "5"
                        template = "dog"
                        posX = 15
                        posY = 26
                        priority = 5
                        enabled = false
                    }
                    ally {
                        template = "scientist"
                        id = "1"
                        group = "0"
                        posX = 2
                        posY = 23
                    }
                    ally {
                        template = "scientist"
                        id = "2"
                        group = "0"
                        posX = 4
                        posY = 23
                    }
                }

                itemBuilders {
                    consumable {
                        id = "health"
                        type = ConsumableType.HEALTH
                    }
                }

                items {
                    consumable {
                        id = "10"
                        template = "health"
                        posX = 4
                        posY = 20
                    }
                }

                triggers {
                    character {
                        id = "523"
                        eventId = "912"
                        targetId = "1"
                        enabled = true
                    }
                    character {
                        id = "525"
                        eventId = "482"
                        targetId = "2"
                        enabled = true
                    }
                }
                events {
                    interactive {
                        id = "912"
                        text = "Welcome to this new game"
                    }
                    swapCharacter {
                        id = "482"
                        enableCharacterId = "5"
                        disableCharacterId = "2"
                        nextEventId = InitialValues.NOOP_ID
                    }
                }
            } ?: return

            val aiRepo = DummyAIRepoImpl(eventLogger)

            val renderer = AWTRenderer(eventLogger, haltUtil, assertUtil)
            val entityManager = EntityManager(
                map,
                sceneConfig.triggerList,
                sceneConfig.eventList,
                sceneConfig.itemList,
                renderer,
                eventLogger,
                aiRepo,
            )

            renderer.startScene(entityManager, sceneConfig, map)
        }
    }
}
